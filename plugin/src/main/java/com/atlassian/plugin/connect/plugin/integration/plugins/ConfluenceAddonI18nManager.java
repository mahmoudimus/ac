package com.atlassian.plugin.connect.plugin.integration.plugins;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.util.resource.AlternativeDirectoryResourceLoader;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

@ConfluenceComponent
public class ConfluenceAddonI18nManager implements InitializingBean, ConnectAddonI18nManager
{
    private static final Logger log = LoggerFactory.getLogger(ConnectAddonI18nManager.class);

    public static final String CACHES_DIR = "connect";
    public static final String I18N_FILE = "connect-addons.properties";
    public static final String ATLASSIAN_CONNECT_I18N_RELOADER = "confluence-atlassian-connect-i18n-reloader";

    private final ApplicationProperties applicationProperties;
    private final Plugin theConnectPlugin;
    private final PluginController pluginController;
    private final ConcurrentHashMap<String, String> i18nProps;

    @Inject
    public ConfluenceAddonI18nManager(ApplicationProperties applicationProperties,
                                      PluginRetrievalService pluginRetrievalService,
                                      PluginController pluginController)
    {
        this.applicationProperties = applicationProperties;
        this.theConnectPlugin = pluginRetrievalService.getPlugin();
        this.pluginController = pluginController;
        this.i18nProps = new ConcurrentHashMap<String, String>();
    }

    /**
     * Register i18n properties for a newly installed plugin.
     */
    public synchronized void add(String addonKey, Properties i18nProperties) throws IOException
    {
        Map<String, String> map = newHashMap();
        for (final String name : i18nProperties.stringPropertyNames())
        {
            map.put(name, i18nProperties.getProperty(name));
        }

        add(addonKey, map);
    }

    @Override
    public synchronized void add(String addonKey, Map<String, String> i18nMap) throws IOException
    {
        i18nProps.putAll(i18nMap);

        Properties propsForFile = new Properties();
        propsForFile.putAll(i18nProps);

        File i18nFile = ensureI18nFileExists();

        FileWriter out = new FileWriter(i18nFile, false);

        try
        {
            propsForFile.store(out, "generated by the Atlassian Connect Plugin");
        }
        finally
        {
            out.close();
        }

        triggerI18nReload();
    }

    private void triggerI18nReload()
    {
        ModuleDescriptor moduleDescriptor = theConnectPlugin.getModuleDescriptor(ATLASSIAN_CONNECT_I18N_RELOADER);
        if (moduleDescriptor != null)
        {
            String moduleKey = moduleDescriptor.getCompleteKey();
            pluginController.disablePluginModule(moduleKey);
            pluginController.enablePluginModule(moduleKey);
            log.debug("Confluence i18n reload triggered");
        }
        else
        {
            log.error("Connect i18n module descriptor not found");
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        ensureI18nFileExists();
        updateSystemProperty();
    }

    private File ensureI18nFileExists() throws IOException
    {
        File cacheDir = getCacheDir();

        if (!cacheDir.exists())
        {
            FileUtils.forceMkdir(cacheDir);
        }

        File i18nFile = new File(cacheDir, I18N_FILE);

        if (!i18nFile.exists())
        {
            FileUtils.touch(i18nFile);
        }

        return i18nFile;
    }

    private void updateSystemProperty() throws IOException
    {
        ArrayList<String> dirs = newArrayList(Splitter.on(",").split(System.getProperty(AlternativeDirectoryResourceLoader.PLUGIN_RESOURCE_DIRECTORIES, "")));
        dirs.add(getCacheDir().getCanonicalPath());

        System.setProperty(AlternativeDirectoryResourceLoader.PLUGIN_RESOURCE_DIRECTORIES, Joiner.on(",").join(dirs));
    }

    private File getCacheDir()
    {
        return new File(applicationProperties.getHomeDirectory(), CACHES_DIR);
    }
}
