package it.com.atlassian.plugin.connect.testlifecycle;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;

import com.atlassian.plugin.*;
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

/**
 * we have our own version of this because we can't use the test support installer due to it's dependency on connect modules
 */
public class LifecyclePluginInstaller implements DisposableBean
{
    public static final String FILTER_MAPPING = "/ac-test-addon";
    public static final String DESCRIPTOR_PREFIX = "connect-descriptor-";
    private ServiceTracker serviceTracker;
    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;
    private final ApplicationProperties applicationProperties;
    private final BundleContext bundleContext;
    private PluginArtifactFactory pluginArtifactFactory = new DefaultPluginArtifactFactory();
    private static final SecureRandom random = new SecureRandom();

    public LifecyclePluginInstaller(PluginController pluginController, PluginAccessor pluginAccessor, ApplicationProperties applicationProperties, BundleContext bundleContext)
    {
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
        this.applicationProperties = applicationProperties;
        this.bundleContext = bundleContext;
    }

    public Plugin installAddon(String json) throws IOException
    {
        File descriptor = createTempDescriptor(json);

        PluginInstallHandler handler = getInstallHandler();

        checkNotNull(handler);

        return handler.installPlugin(descriptor, Option.<String>some("application/json")).getPlugin();
    }

    public Plugin installPlugin(File jarFile) throws IOException
    {
        PluginArtifact artifact = pluginArtifactFactory.create(jarFile.toURI());
        String key = pluginController.installPlugins(artifact).iterator().next();
        return pluginAccessor.getPlugin(key);
    }

    public void uninstallPlugin(Plugin plugin) throws IOException
    {
        pluginController.uninstall(plugin);
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
        ServiceTracker tracker = getServiceTracker();

        checkNotNull(tracker);
        for (ServiceReference ref : tracker.getServiceReferences())
        {
            PluginInstallHandler handler = (PluginInstallHandler) tracker.getService(ref);
            if (handler.getClass().getName().contains("ConnectUPMInstallHandler"))
            {
                return handler;
            }
        }

        return null;
    }

    private ServiceTracker getServiceTracker()
    {
        if (null == serviceTracker)
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

    public static String randomName(String base)
    {
        long n = random.nextLong();
        n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);

        return base + Long.toString(n);
    }
}
