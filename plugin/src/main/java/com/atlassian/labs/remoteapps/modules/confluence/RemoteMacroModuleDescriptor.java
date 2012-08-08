package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.labs.remoteapps.RemoteAppAccessorFactory;
import com.atlassian.labs.remoteapps.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

/**
 * Generates a macro that retrieves its contents via a remote call
 */
public class RemoteMacroModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final MacroModuleDescriptorCreator.Builder macroModuleDescriptorCreatorBuilder;

    private Element descriptor;

    public RemoteMacroModuleDescriptor(
            DynamicDescriptorRegistration dynamicDescriptorRegistration,
            MacroModuleDescriptorCreator macroModuleDescriptorCreator,
            final MacroContentManager macroContentManager,
            final RemoteAppAccessorFactory remoteAppAccessorFactory,
            final WebResourceManager webResourceManager)
    {
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.macroModuleDescriptorCreatorBuilder = macroModuleDescriptorCreator.newBuilder()
            .setMacroFactory(new MacroModuleDescriptorCreator.MacroFactory()
            {
                @Override
                public RemoteMacro create(RemoteMacroInfo remoteMacroInfo)
                {
                    return new StorageFormatMacro(remoteMacroInfo,
                            macroContentManager,
                            remoteAppAccessorFactory,
                            webResourceManager);
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
        dynamicDescriptorRegistration.registerDescriptors(getPlugin(),
                macroModuleDescriptorCreatorBuilder.build(getPlugin(),  descriptor));
    }
}
