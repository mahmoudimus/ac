package com.atlassian.plugin.connect.plugin.module.provider;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Options;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.LifecycleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.plugin.webhooks.PluginsWebHookProvider;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebHookModuleBean.newWebHookBean;

@Component
public class ModuleListProviderContainerImpl implements ModuleListProviderContainer
{
    private static final Logger log = Logger.getLogger(ModuleListProviderContainerImpl.class);
    private static final String WEBHOOKS_FIELD = "webhooks";

    private final PluginAccessor pluginAccessor;
    private final ContainerManagedPlugin theConnectPlugin;

    @Autowired
    public ModuleListProviderContainerImpl(PluginAccessor pluginAccessor, PluginRetrievalService pluginRetrievalService)
    {
        this.pluginAccessor = pluginAccessor;
        this.theConnectPlugin = (ContainerManagedPlugin) pluginRetrievalService.getPlugin();
    }

    @Override
    public Iterable<ModuleListProviderFactory> provideFactories(final ConnectAddonBean addon)
    {
        final List<ConnectModuleListProviderModuleDescriptor> providers =
                pluginAccessor.getEnabledModuleDescriptorsByClass(ConnectModuleListProviderModuleDescriptor.class);

        final Iterable<Option<ModuleListProviderFactory>> moduleListProviderFactories = Iterables.transform(providers, new Function<ConnectModuleListProviderModuleDescriptor, Option<ModuleListProviderFactory>>()
        {
            @Override
            public Option<ModuleListProviderFactory> apply(final ConnectModuleListProviderModuleDescriptor descriptor)
            {
                if (descriptor.getPlugin() instanceof ContainerManagedPlugin)
                {
                    final ContainerManagedPlugin plugin = (ContainerManagedPlugin) descriptor.getPlugin();
                    return Option.<ModuleListProviderFactory>some(new ModuleListProviderFactoryImpl(descriptor.getModule(), plugin));
                }
                else
                {
                    log.warn("The plugin " + descriptor.getPlugin().getKey() + ", which provides module list is not managed by a container. ");
                    return Option.none();
                }
            }
        });

        final ImmutableList.Builder<ModuleListProviderFactory> builder = ImmutableList.builder();

        builder.add(new ModuleListProviderFactoryImpl(buildCoreConnectModuleList(addon), theConnectPlugin));
        builder.addAll(Options.flatten(Options.filterNone(moduleListProviderFactories)));

        return builder.build();
    }

    private ModuleList buildCoreConnectModuleList(final ConnectAddonBean addon)
    {
        LifecycleBean lifecycle = addon.getLifecycle();
        ConnectAddonBeanBuilder builder = newConnectAddonBean(addon);

        if (!Strings.isNullOrEmpty(lifecycle.getEnabled()))
        {
            //add webhook
            builder.withModule(WEBHOOKS_FIELD, newWebHookBean().withEvent(PluginsWebHookProvider.CONNECT_ADDON_ENABLED).withUrl(lifecycle.getEnabled()).build());
        }
        if (!Strings.isNullOrEmpty(lifecycle.getDisabled()))
        {
            //add webhook
            builder.withModule(WEBHOOKS_FIELD, newWebHookBean().withEvent(PluginsWebHookProvider.CONNECT_ADDON_DISABLED).withUrl(lifecycle.getDisabled()).build());
        }
        if (!Strings.isNullOrEmpty(lifecycle.getUninstalled()))
        {
            //add webhook
            builder.withModule(WEBHOOKS_FIELD, newWebHookBean().withEvent(PluginsWebHookProvider.CONNECT_ADDON_UNINSTALLED).withUrl(lifecycle.getUninstalled()).build());
        }

        return builder.build().getModules();
    }
}
