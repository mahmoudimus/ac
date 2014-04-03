package com.atlassian.plugin.connect.plugin.integration.plugins;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.atlassian.config.HomeLocator;
import com.atlassian.plugin.*;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.atlassian.plugin.util.resource.AlternativeDirectoryResourceLoader;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.util.concurrent.CopyOnWriteMap;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.plugin.util.BundleUtil.findBundleWithName;
import static com.atlassian.plugin.connect.plugin.util.BundleUtil.toBundleNames;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.abs;

/**
 * This class loads a plugin called "atlassian-connect.i18n", which solely exists to store and expose generated i18n files
 * from remote descriptors.  Ideally, the products would support generated i18n resources, but currently they expect
 * them to be declared via <resource> tags pointing at physical files in the plugin.  This plugin is a way to get
 * around that.
 */
@Component
public class I18nPropertiesPluginManager implements InitializingBean
{
    private static final Logger log = LoggerFactory.getLogger(I18nPropertiesPluginManager.class);

    public static final String CACHES_DIR = "caches";
    public static final String I18N_FILE = "connect-addons.properties";
            
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Map<String, String> i18nToRegister = CopyOnWriteMap.newHashMap();
    private final ApplicationProperties applicationProperties;

    @Autowired
    public I18nPropertiesPluginManager(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Register i18n properties for a newly installed plugin.
     */
    public synchronized void add(String addonKey, Properties i18nProperties) throws IOException
    {
        File i18nFile = ensureI18nFileExists();

        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(i18nFile,true)));

        i18nProperties.list(out);
        
        IOUtils.closeQuietly(out);
    }

    private File ensureI18nFileExists() throws IOException
    {
        File cacheDir = getCacheDir();
        
        if(!cacheDir.exists())
        {
            FileUtils.forceMkdir(cacheDir);
        }
        
        File i18nFile = new File(cacheDir,I18N_FILE);
        
        if(!i18nFile.exists())
        {
            FileUtils.touch(i18nFile);
        }
        
        return i18nFile;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        ensureI18nFileExists();
        updateSystemProperty();
    }

    private void updateSystemProperty() throws IOException
    {
        ArrayList<String> dirs = newArrayList(Splitter.on(",").split(System.getProperty(AlternativeDirectoryResourceLoader.PLUGIN_RESOURCE_DIRECTORIES, "")));
        dirs.add(getCacheDir().getCanonicalPath());
        
        System.setProperty(AlternativeDirectoryResourceLoader.PLUGIN_RESOURCE_DIRECTORIES, Joiner.on(",").join(dirs));
    }
    
    private File getCacheDir()
    {
        return new File(applicationProperties.getHomeDirectory(),CACHES_DIR);
    }
}
