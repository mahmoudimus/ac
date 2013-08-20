package com.atlassian.plugin.connect.plugin.module.confluence;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.util.concurrent.NotNull;

import org.dom4j.Element;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates a macro that retrieves its contents via a remote call
 */
public final class RemoteMacroModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final MacroModuleDescriptorCreator.Builder macroModuleDescriptorCreatorBuilder;

    private Element descriptor;
    private DynamicDescriptorRegistration.Registration registration;

    public RemoteMacroModuleDescriptor(
            ModuleFactory moduleFactory,
            DynamicDescriptorRegistration dynamicDescriptorRegistration,
            MacroModuleDescriptorCreator macroModuleDescriptorCreator,
            MacroContentManager macroContentManager,
            DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory)
    {
        super(moduleFactory);
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
        this.macroModuleDescriptorCreatorBuilder = newMacroModuleDescriptorCreatorBuilder(
                checkNotNull(macroModuleDescriptorCreator),
                checkNotNull(macroContentManager),
                checkNotNull(remotablePluginAccessorFactory));
    }

    private static MacroModuleDescriptorCreator.Builder newMacroModuleDescriptorCreatorBuilder(final MacroModuleDescriptorCreator macroModuleDescriptorCreator,
                                                                                               final MacroContentManager macroContentManager,
                                                                                               final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory)
    {
        return macroModuleDescriptorCreator.newBuilder().setMacroFactory(new MacroModuleDescriptorCreator.MacroFactory()
        {
            @Override
            public RemoteMacro create(RemoteMacroInfo remoteMacroInfo)
            {
                return new StorageFormatMacro(remoteMacroInfo, macroContentManager, remotablePluginAccessorFactory);
            }
        });
    }

    @Override
    public Void getModule()
    {
        return null;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.descriptor = element;
    }

    @Override
    public void enabled()
    {
        super.enabled();
        this.registration = dynamicDescriptorRegistration.registerDescriptors(getPlugin(),
                macroModuleDescriptorCreatorBuilder.build(getPlugin(), descriptor));
    }

    @Override
    public void disabled()
    {
        super.disabled();
        if (registration != null)
        {
            registration.unregister();
        }
    }
}
