package com.atlassian.plugin.connect.testsupport;

import java.io.File;
import java.io.IOException;

import com.atlassian.plugin.*;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.modules.util.ModuleKeyGenerator;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilter;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginInstallHandler;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.beans.factory.DisposableBean;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultTestPluginInstaller implements TestPluginInstaller, DisposableBean
{
    public static final String DESCRIPTOR_PREFIX = "connect-descriptor-";
    private ServiceTracker serviceTracker;
    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;
    private final ApplicationProperties applicationProperties;
    private final BundleContext bundleContext;
    private PluginArtifactFactory pluginArtifactFactory = new DefaultPluginArtifactFactory();

    public DefaultTestPluginInstaller(PluginController pluginController, PluginAccessor pluginAccessor, ApplicationProperties applicationProperties, BundleContext bundleContext)
    {
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
        this.applicationProperties = applicationProperties;
        this.bundleContext = bundleContext;
    }

    @Override
    public Plugin installPlugin(ConnectAddonBean bean) throws IOException
    {
        String json = ConnectModulesGsonFactory.addonBeanToJson(bean);
        return installPlugin(json);
    }

    @Override
    public Plugin installPlugin(String jsonDescriptor) throws IOException
    {
        File descriptor = createTempDescriptor(jsonDescriptor);

        PluginInstallHandler handler = getInstallHandler();

        checkNotNull(handler);

        return handler.installPlugin(descriptor, Option.<String>some("application/json")).getPlugin();
    }

    @Override
    public Plugin installPlugin(File jarFile) throws IOException
    {
        PluginArtifact artifact = pluginArtifactFactory.create(jarFile.toURI());
        String key = pluginController.installPlugins(artifact).iterator().next();
        return pluginAccessor.getPlugin(key);
    }

    @Override
    public void uninstallPlugin(Plugin plugin) throws IOException
    {
        pluginController.uninstall(plugin);
    }

    @Override
    public void disablePlugin(String pluginKey) throws IOException
    {
        pluginController.disablePlugin(pluginKey);
    }

    @Override
    public void enablePlugin(String pluginKey) throws IOException
    {
        pluginController.enablePlugins(pluginKey);
    }

    @Override
    public String getInternalAddonBaseUrl(String pluginKey)
    {
        String productBase = applicationProperties.getBaseUrl(UrlMode.CANONICAL);

        if(productBase.endsWith("/"))
        {
            productBase = StringUtils.chop(productBase);
        }

        return productBase + AddonTestFilter.FILTER_MAPPING + "/" + pluginKey;
    }

    private File createTempDescriptor(String json) throws IOException
    {
        File tmpFile = File.createTempFile(ModuleKeyGenerator.randomName(DESCRIPTOR_PREFIX), ".json");
        Files.write(json,tmpFile, Charsets.UTF_8);

        return tmpFile;
    }

    private PluginInstallHandler getInstallHandler()
    {
        /*
        NOTE: we have to get the handler via OSGi by it's string name because we can't depend on the connect plugin.
         */
        ServiceTracker tracker = getServiceTracker();

        checkNotNull(tracker);
        for(ServiceReference ref : tracker.getServiceReferences())
        {
            PluginInstallHandler handler = (PluginInstallHandler) tracker.getService(ref);
            if(handler.getClass().getName().contains("ConnectUPMInstallHandler"))
            {
                return handler;
            }
        }

        return null;
    }

    private ServiceTracker getServiceTracker()
    {
        if(null == serviceTracker)
        {
            synchronized (this)
            {
                ServiceTracker tracker = new ServiceTracker(bundleContext, PluginInstallHandler.class.getName(), null);
                tracker.open();
                this.serviceTracker = tracker;
            }
        }

        return serviceTracker;
    }

    @Override
    public void destroy() throws Exception
    {
        serviceTracker.close();
    }
}
