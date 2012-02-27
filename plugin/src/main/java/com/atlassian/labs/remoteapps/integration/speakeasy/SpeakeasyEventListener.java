package com.atlassian.labs.remoteapps.integration.speakeasy;

import com.atlassian.event.api.EventListener;
import com.atlassian.labs.remoteapps.event.RemoteAppStartedEvent;
import com.atlassian.labs.remoteapps.event.RemoteAppStoppedEvent;
import com.atlassian.labs.remoteapps.util.BundleUtil;
import com.atlassian.labs.speakeasy.descriptor.external.ConditionGenerator;
import com.atlassian.labs.speakeasy.descriptor.external.DescriptorGenerator;
import com.atlassian.labs.speakeasy.external.SpeakeasyBackendService;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.LegacyModuleFactory;
import org.dom4j.DocumentFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.util.Collections;

/**
 * Marks apps as Speakeasy apps so they show up as global extensions in the UI
 */
public class SpeakeasyEventListener
{
    private final SpeakeasyBackendService speakeasyBackendService;
    private final BundleContext bundleContext;
    private final PluginAccessor pluginAccessor;

    public SpeakeasyEventListener(BundleContext bundleContext,
            PluginAccessor pluginAccessor, SpeakeasyBackendService speakeasyBackendService)
    {
        this.bundleContext = bundleContext;
        this.pluginAccessor = pluginAccessor;
        this.speakeasyBackendService = speakeasyBackendService;
    }

    @EventListener
    public void onAppStarted(RemoteAppStartedEvent event)
    {
        // ensure the app is visible by speakeasy
        String remoteAppKey = event.getRemoteAppKey();
        Bundle appBundle = BundleUtil.findBundleForPlugin(bundleContext, remoteAppKey);
        appBundle.getBundleContext().registerService(
                ModuleDescriptor.class.getName(),
                new SpeakeasyMarkerModuleDescriptor(pluginAccessor.getPlugin(remoteAppKey)),
                null);

        // It ensures any remote apps installed after
        // Speakeasy 1.3.15 will show up as globally-enabled extensions correctly.
        if (!speakeasyBackendService.isGlobalExtension(remoteAppKey))
        {
            speakeasyBackendService.addGlobalExtension(remoteAppKey);
        }
    }
    
    @EventListener
    public void onAppStopped(RemoteAppStoppedEvent event)
    {
    }

    public static class SpeakeasyMarkerModuleDescriptor extends AbstractModuleDescriptor implements
            DescriptorGenerator
    {
        public SpeakeasyMarkerModuleDescriptor(Plugin plugin)
        {
            super(new LegacyModuleFactory());
            init(plugin, DocumentFactory.getInstance().createElement("marker")
                    .addAttribute("key", "__speakeasy_marker"));
        }

        @Override
        public Object getModule()
        {
            return null;
        }

        @Override
        public Iterable getDescriptorsToExposeForUsers(ConditionGenerator conditionGenerator,
                long l)
        {
            return Collections.emptyList();
        }
    }
}
