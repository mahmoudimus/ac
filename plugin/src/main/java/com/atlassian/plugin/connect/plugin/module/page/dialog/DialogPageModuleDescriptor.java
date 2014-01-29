package com.atlassian.plugin.connect.plugin.module.page.dialog;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.page.RemotePageDescriptorCreator;
import com.atlassian.util.concurrent.NotNull;

import org.dom4j.Element;

import static com.atlassian.plugin.connect.plugin.iframe.servlet.ConnectIFrameServlet.RAW_CLASSIFIER;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates a dialog page with a servlet containing an iframe and a web item
 */
public class DialogPageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final RemotePageDescriptorCreator.Builder remotePageDescriptorBuilder;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    private Element descriptor;
    private DynamicDescriptorRegistration.Registration registration;

    public DialogPageModuleDescriptor(
            ModuleFactory moduleFactory,
            DynamicDescriptorRegistration dynamicDescriptorRegistration,
            RemotePageDescriptorCreator remotePageDescriptorCreator, final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory, final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        super(moduleFactory);
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
        this.remotePageDescriptorBuilder = checkNotNull(remotePageDescriptorCreator).newBuilder()
                .setTemplateSuffix("-dialog")
                .setWebItemStyleClass("ap-dialog");

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
                remotePageDescriptorBuilder.build(getPlugin(), descriptor));

        // ACDEV-886 -- register a render strategy for the dialog page, so that JS on the client doesn't
        // have to care whether this dialog was registered as an XML or JSON descriptor
        String key = getRequiredAttribute(descriptor, "key");
        String url = getRequiredAttribute(descriptor, "url");
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(plugin.getKey())
                .module(key)
                .dialogTemplate()
                .urlTemplate(url)
                .build();
        iFrameRenderStrategyRegistry.register(plugin.getKey(), key, RAW_CLASSIFIER, renderStrategy);
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
