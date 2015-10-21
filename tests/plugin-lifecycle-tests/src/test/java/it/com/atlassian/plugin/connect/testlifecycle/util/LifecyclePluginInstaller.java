package it.com.atlassian.plugin.connect.testlifecycle.util;

import com.atlassian.plugin.DefaultPluginArtifactFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.PluginArtifactFactory;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginControlHandler;
import com.atlassian.upm.spi.PluginInstallHandler;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.beans.factory.DisposableBean;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * we have our own version of this because we can't use the test support installer due to it's dependency on connect modules
 */
public class LifecyclePluginInstaller implements DisposableBean
{

    public static final String FILTER_MAPPING = "/ac-test-addon";
    public static final String DESCRIPTOR_PREFIX = "connect-descriptor-";

    private ServiceTracker installServiceTracker;
    private ServiceTracker controlServiceTracker;
    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;
    private final ApplicationProperties applicationProperties;
    private final BundleContext bundleContext;
    private PluginRetrievalService pluginRetrievalService;
    private PluginArtifactFactory pluginArtifactFactory = new DefaultPluginArtifactFactory();
    private static final SecureRandom random = new SecureRandom();
    private Map<String, File> jarDependencies = Maps.newConcurrentMap();

    public LifecyclePluginInstaller(PluginController pluginController,
            PluginAccessor pluginAccessor,
            ApplicationProperties applicationProperties,
            BundleContext bundleContext,
            PluginRetrievalService pluginRetrievalService)
    {
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
        this.applicationProperties = applicationProperties;
        this.bundleContext = bundleContext;
        this.pluginRetrievalService = pluginRetrievalService;
    }

    public Plugin installAddon(ConnectAddonBean bean) throws IOException
    {
        String json = ConnectModulesGsonFactory.addonBeanToJson(bean);
        return installAddon(json);
    }

    public Plugin installAddon(String jsonDescriptor) throws IOException
    {
        File descriptor = createTempDescriptor(jsonDescriptor);

        PluginInstallHandler handler = getInstallHandler();

        checkNotNull(handler);

        return handler.installPlugin(descriptor, Option.<String>some("application/json")).getPlugin();
    }

    public Plugin installConnectPlugin() throws IOException, URISyntaxException
    {
        return installPlugin(getConnectPluginJarFilename());
    }

    public Plugin installJiraReferencePlugin() throws IOException, URISyntaxException
    {
        return installPlugin(getJiraReferencePluginJarFilename());
    }

    public Plugin installPlugin(String jarFilename) throws IOException, URISyntaxException
    {
        File jarFile = getJarFile(jarFilename);
        PluginArtifact artifact = pluginArtifactFactory.create(jarFile.toURI());
        String pluginKey = pluginController.installPlugins(artifact).iterator().next();
        return pluginAccessor.getPlugin(pluginKey);
    }

    public void uninstallAddon(Plugin plugin) throws IOException
    {
        PluginControlHandler handler = getControlHandler();

        checkNotNull(handler);

        handler.uninstall(plugin);
    }

    public void uninstallPlugin(Plugin plugin) throws IOException
    {
        pluginController.uninstall(plugin);
    }

    public void disableAddon(String pluginKey) throws IOException
    {
        PluginControlHandler handler = getControlHandler();

        checkNotNull(handler);

        handler.disablePlugin(pluginKey);
    }

    public void enableAddon(String pluginKey) throws IOException
    {
        PluginControlHandler handler = getControlHandler();

        checkNotNull(handler);

        handler.enablePlugins(pluginKey);
    }

    public void disablePlugin(String pluginKey) throws IOException
    {
        pluginController.disablePlugin(pluginKey);
    }

    public void enablePlugin(String pluginKey) throws IOException
    {
        pluginController.enablePlugins(pluginKey);
    }

    public String getInternalAddonBaseUrl(String pluginKey)
    {
        String productBase = applicationProperties.getBaseUrl(UrlMode.CANONICAL);

        if (productBase.endsWith("/"))
        {
            productBase = StringUtils.chop(productBase);
        }

        return productBase + FILTER_MAPPING + "/" + pluginKey;
    }

    public Iterable<String> getInstalledAddonKeys()
    {
        PluginControlHandler handler = getControlHandler();

        checkNotNull(handler);

        return Iterables.transform(handler.getPlugins(), new Function<Plugin, String>()
        {
            @Override
            public String apply(@Nullable Plugin input)
            {
                return (null != input) ? input.getKey() : null;
            }
        });
    }
    
    public Plugin getAddonPlugin(String addonKey)
    {
        PluginControlHandler handler = getControlHandler();

        checkNotNull(handler);
        
        return handler.getPlugin(addonKey);
    }

    private File createTempDescriptor(String json) throws IOException
    {
        File tmpFile = File.createTempFile(randomName(DESCRIPTOR_PREFIX), ".json");
        Files.write(json, tmpFile, Charsets.UTF_8);

        return tmpFile;
    }

    private PluginInstallHandler getInstallHandler()
    {
        /*
        NOTE: we have to get the handler via OSGi by it's string name because we can't depend on the connect plugin.
         */
        if (null == installServiceTracker)
        {
            installServiceTracker = getServiceTracker(PluginInstallHandler.class);
        }


        checkNotNull(installServiceTracker);
        for (ServiceReference ref : installServiceTracker.getServiceReferences())
        {
            Object service = installServiceTracker.getService(ref);
            if (service.getClass().getName().contains("ConnectUPMInstallHandler"))
            {
                return (PluginInstallHandler) service;
            }
        }

        return null;
    }

    private PluginControlHandler getControlHandler()
    {
        /*
        NOTE: we have to get the handler via OSGi by it's string name because we can't depend on the connect plugin.
         */
        if (null == controlServiceTracker)
        {
            controlServiceTracker = getServiceTracker(PluginControlHandler.class);
        }

        checkNotNull(controlServiceTracker);
        ServiceReference[] serviceReferences = controlServiceTracker.getServiceReferences();

        if (null != serviceReferences)
        {
            for (ServiceReference ref : serviceReferences)
            {
                Object service = controlServiceTracker.getService(ref);
                if (service.getClass().getName().contains("ConnectUPMControlHandler"))
                {
                    return (PluginControlHandler) service;
                }
            }
        }

        return null;
    }

    private ServiceTracker getServiceTracker(Class clazz)
    {
        ServiceTracker tracker;

        synchronized (this)
        {
            tracker = new ServiceTracker(bundleContext, clazz.getName(), null);
            tracker.open();
        }

        return tracker;
    }

    @Override
    public void destroy() throws Exception
    {
        if (null != controlServiceTracker)
        {
            controlServiceTracker.close();
        }

        if (null != installServiceTracker)
        {
            installServiceTracker.close();
        }
    }

    private String getConnectPluginJarFilename() throws IOException
    {
        return getPluginDependencyFilename("atlassian-connect-plugin");
    }

    private String getJiraReferencePluginJarFilename() throws IOException
    {
        return getPluginDependencyFilename("atlassian-connect-jira-reference-plugin");
    }

    private String getPluginDependencyFilename(String artifactId) throws IOException
    {
        return String.format("/%s-%s.jar", artifactId, getPluginVersion());
    }

    private File getJarFile(String jarFilename)
    {
        return jarDependencies.computeIfAbsent(jarFilename, new java.util.function.Function<String, File>()
        {
            @Override
            public File apply(String filename)
            {
                try
                {
                    File tempFile = File.createTempFile(filename, ".jar");
                    URL jarResource = getClass().getResource("/" + filename);
                    FileUtils.copyURLToFile(jarResource, tempFile);
                    return tempFile;
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private String getPluginVersion()
    {
        return pluginRetrievalService.getPlugin().getPluginInformation().getVersion();
    }

    public static String randomName(String base)
    {
        long n = random.nextLong();
        n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);

        return base + Long.toString(n);
    }
}
