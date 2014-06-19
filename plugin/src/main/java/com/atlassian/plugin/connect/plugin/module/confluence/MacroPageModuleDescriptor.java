package com.atlassian.plugin.connect.plugin.module.confluence;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.plugin.integration.plugins.LegacyXmlDynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploder;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates a macro page with an iframe
 */
public final class MacroPageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    @XmlDescriptor
    private final LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final MacroModuleDescriptorCreator.Builder macroModuleDescriptorCreatorBuilder;

    private Element descriptor;
    @XmlDescriptor
    private LegacyXmlDynamicDescriptorRegistration.Registration registration;

    public MacroPageModuleDescriptor(
            ModuleFactory moduleFactory,
            LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration,
            MacroModuleDescriptorCreator macroModuleDescriptorCreator,
            final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory,
            final UserManager userManager,
            final IFrameRendererImpl iFrameRenderer)
    {
        super(moduleFactory);
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
        this.macroModuleDescriptorCreatorBuilder = checkNotNull(macroModuleDescriptorCreator).newBuilder()
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
        XmlDescriptorExploder.notifyAndExplode(getPluginKey());

        super.enabled();
        this.registration = dynamicDescriptorRegistration.registerDescriptors(getPlugin(),
                macroModuleDescriptorCreatorBuilder.build(getPlugin(), descriptor));
    }

    @Override
    public void disabled()
    {
        XmlDescriptorExploder.notifyAndExplode(getPluginKey());

        super.disabled();
        if (registration != null)
        {
            registration.unregister();
        }
    }

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }
}
