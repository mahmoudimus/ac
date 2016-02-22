package com.atlassian.plugin.connect.testsupport;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilter;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.spi.PluginControlHandler;
import com.atlassian.upm.spi.PluginInstallHandler;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.beans.factory.DisposableBean;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultTestPluginInstaller implements TestPluginInstaller, DisposableBean {
    public static final String DESCRIPTOR_PREFIX = "connect-descriptor-";
    private ServiceTracker installServiceTracker;
    private ServiceTracker controlServiceTracker;
    private final ApplicationProperties applicationProperties;
    private final BundleContext bundleContext;

    public DefaultTestPluginInstaller(ApplicationProperties applicationProperties, BundleContext bundleContext) {
        this.applicationProperties = applicationProperties;
        this.bundleContext = bundleContext;
    }

    @Override
    public Plugin installAddon(ConnectAddonBean bean) throws IOException {
        String json = ConnectModulesGsonFactory.addonBeanToJson(bean);
        return installAddon(json);
    }

    @Override
    public Plugin installAddon(String jsonDescriptor) throws IOException {
        File descriptor = createTempDescriptor(jsonDescriptor);
        PluginInstallHandler handler = getInstallHandler();

        checkNotNull(handler);

        return handler.installPlugin(descriptor, Option.some("application/json")).getPlugin();
    }

    @Override
    public void uninstallAddon(Plugin plugin) throws IOException {
        PluginControlHandler handler = getControlHandler();

        checkNotNull(handler);

        handler.uninstall(plugin);
    }

    @Override
    public void uninstallAddon(String pluginKey) throws IOException {
        PluginControlHandler handler = getControlHandler();

        checkNotNull(handler);

        Plugin plugin = handler.getPlugin(pluginKey);
        uninstallAddon(plugin);
    }

    @Override
    public void disableAddon(String pluginKey) throws IOException {
        PluginControlHandler handler = getControlHandler();

        checkNotNull(handler);

        handler.disablePlugin(pluginKey);
    }

    @Override
    public void enableAddon(String pluginKey) throws IOException {
        PluginControlHandler handler = getControlHandler();

        checkNotNull(handler);

        handler.enablePlugins(pluginKey);
    }

    @Override
    public String getInternalAddonBaseUrl(String pluginKey) {
        String productBase = applicationProperties.getBaseUrl(UrlMode.CANONICAL);

        if (productBase.endsWith("/")) {
            productBase = StringUtils.chop(productBase);
        }

        return productBase + AddonTestFilter.FILTER_MAPPING + "/" + pluginKey;
    }

    @Override
    public String getInternalAddonBaseUrlSuffix(String pluginKey, String additionalSuffix) {
        return AddonTestFilter.FILTER_MAPPING + '/' + pluginKey + additionalSuffix;
    }

    @Override
    public Iterable<String> getInstalledAddonKeys() {
        PluginControlHandler handler = getControlHandler();

        checkNotNull(handler);

        return Iterables.transform(handler.getPlugins(), new Function<Plugin, String>() {
            @Override
            public String apply(@Nullable Plugin input) {
                return (null != input) ? input.getKey() : null;
            }
        });
    }

    private File createTempDescriptor(String json) throws IOException {
        File tmpFile = File.createTempFile(ModuleKeyUtils.randomName(DESCRIPTOR_PREFIX), ".json");
        Files.write(json, tmpFile, Charsets.UTF_8);

        return tmpFile;
    }

    private PluginInstallHandler getInstallHandler() {
        /*
        NOTE: we have to get the handler via OSGi by it's string name because we can't depend on the connect plugin.
         */
        if (null == installServiceTracker) {
            installServiceTracker = getServiceTracker(PluginInstallHandler.class);
        }


        checkNotNull(installServiceTracker);
        for (ServiceReference ref : installServiceTracker.getServiceReferences()) {
            Object service = installServiceTracker.getService(ref);
            if (service.getClass().getName().contains("ConnectUPMInstallHandler")) {
                return (PluginInstallHandler) service;
            }
        }

        return null;
    }

    private PluginControlHandler getControlHandler() {
        /*
        NOTE: we have to get the handler via OSGi by it's string name because we can't depend on the connect plugin.
         */
        if (null == controlServiceTracker) {
            controlServiceTracker = getServiceTracker(PluginControlHandler.class);
        }

        checkNotNull(controlServiceTracker);
        for (ServiceReference ref : controlServiceTracker.getServiceReferences()) {
            Object service = controlServiceTracker.getService(ref);
            if (service.getClass().getName().contains("ConnectUPMControlHandler")) {
                return (PluginControlHandler) service;
            }
        }

        return null;
    }

    private ServiceTracker getServiceTracker(Class clazz) {
        ServiceTracker tracker;

        synchronized (this) {
            tracker = new ServiceTracker(bundleContext, clazz.getName(), null);
            tracker.open();
        }

        return tracker;
    }

    @Override
    public void destroy() throws Exception {
        if (null != controlServiceTracker) {
            controlServiceTracker.close();
        }

        if (null != installServiceTracker) {
            installServiceTracker.close();
        }
    }
}
