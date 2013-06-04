package com.atlassian.plugin.remotable.plugin.integration.speakeasy;

import com.atlassian.event.api.EventListener;
import com.atlassian.labs.speakeasy.descriptor.external.ConditionGenerator;
import com.atlassian.labs.speakeasy.descriptor.external.DescriptorGenerator;
import com.atlassian.labs.speakeasy.external.SpeakeasyBackendService;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.remotable.host.common.util.BundleUtil;
import org.dom4j.DocumentFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.util.Collections;

import static com.atlassian.plugin.remotable.host.common.util.RemotablePluginManifestReader.isRemotePlugin;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Marks apps as Speakeasy apps so they show up as global extensions in the UI
 */
public class SpeakeasyEventListener
{
    private final ModuleFactory moduleFactory;
    private final SpeakeasyBackendService speakeasyBackendService;
    private final BundleContext bundleContext;

    public SpeakeasyEventListener(ModuleFactory moduleFactory, BundleContext bundleContext, SpeakeasyBackendService speakeasyBackendService)
    {
        this.moduleFactory = checkNotNull(moduleFactory);
        this.bundleContext = checkNotNull(bundleContext);
        this.speakeasyBackendService = checkNotNull(speakeasyBackendService);
    }

    @EventListener
    public void onAppStarted(PluginEnabledEvent event)
    {
        Bundle bundle = BundleUtil.findBundleForPlugin(bundleContext, event.getPlugin().getKey());
        if (bundle != null && isRemotePlugin(bundle))
        {
            makeAppVisibleInSpeakeasy(bundle, event.getPlugin());
        }
    }

    private void makeAppVisibleInSpeakeasy(Bundle bundle, Plugin plugin)
    {
        // ensure the app is visible by speakeasy
        bundle.getBundleContext().registerService(
                ModuleDescriptor.class.getName(),
                new SpeakeasyMarkerModuleDescriptor(moduleFactory, plugin),
                null);

        // It ensures any remotable plugins installed after
        // Speakeasy 1.3.15 will show up as globally-enabled extensions correctly.
        if (!speakeasyBackendService.isGlobalExtension(plugin.getKey()))
        {
            speakeasyBackendService.addGlobalExtension(plugin.getKey());
        }
    }

    public static final class SpeakeasyMarkerModuleDescriptor extends AbstractModuleDescriptor implements DescriptorGenerator
    {
        public SpeakeasyMarkerModuleDescriptor(ModuleFactory moduleFactory, Plugin plugin)
        {
            super(moduleFactory);
            init(plugin, DocumentFactory.getInstance().createElement("marker").addAttribute("key", "__speakeasy_marker"));
        }

        @Override
        public Object getModule()
        {
            return null;
        }

        @Override
        public Iterable getDescriptorsToExposeForUsers(ConditionGenerator conditionGenerator, long l)
        {
            return Collections.emptyList();
        }
    }
}
