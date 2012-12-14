package com.atlassian.plugin.remotable.plugin.module.confluence;

import com.atlassian.plugin.remotable.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.remotable.spi.module.IFrameParams;
import com.atlassian.plugin.remotable.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.remotable.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.remotable.spi.module.IFrameContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.remotable.plugin.module.page.IFrameContextImpl;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

/**
 * Generates a macro page with an iframe
 */
public class MacroPageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final MacroModuleDescriptorCreator.Builder macroModuleDescriptorCreatorBuilder;

    private Element descriptor;

    public MacroPageModuleDescriptor(
            DynamicDescriptorRegistration dynamicDescriptorRegistration,
            MacroModuleDescriptorCreator macroModuleDescriptorCreator,
            final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory,
            final UserManager userManager,
            final IFrameRendererImpl iFrameRenderer)
    {
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.macroModuleDescriptorCreatorBuilder = macroModuleDescriptorCreator.newBuilder()
            .setMacroFactory(new MacroModuleDescriptorCreator.MacroFactory()
            {
                @Override
                public RemoteMacro create(RemoteMacroInfo remoteMacroInfo)
                {
                    String moduleKey = remoteMacroInfo.getElement().attributeValue("key");
                    IFrameParams params = new IFrameParamsImpl(remoteMacroInfo.getElement());
                    IFrameContext iFrameContext = new IFrameContextImpl(
                            getPluginKey(),
                            remoteMacroInfo.getUrl(),
                            moduleKey,
                            params
                    );
                    return new PageMacro(remoteMacroInfo, userManager, iFrameRenderer, iFrameContext,
                            remotablePluginAccessorFactory);
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
