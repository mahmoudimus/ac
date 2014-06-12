package com.atlassian.plugin.connect.plugin.module.page.dialog;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.integration.plugins.LegacyXmlDynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.page.RemotePageDescriptorCreator;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates a dialog page with a servlet containing an iframe and a web item
 */
public class DialogPageModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    public static final String DIALOG_CLASSIFIER = "dialog";
    public static final String SIMPLE_DIALOG_CLASSIFIER = "simpleDialog";

    @XmlDescriptor
    private final LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final RemotePageDescriptorCreator.Builder remotePageDescriptorBuilder;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    private Element descriptor;
    @XmlDescriptor
    private LegacyXmlDynamicDescriptorRegistration.Registration registration;

    public DialogPageModuleDescriptor(
            ModuleFactory moduleFactory,
            LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration,
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

        // ACDEV-886 -- register render strategies for the dialog page, so that JS on the client doesn't
        // have to care whether this dialog was registered as an XML or JSON descriptor

        String key = getRequiredAttribute(descriptor, "key");
        String url = getRequiredAttribute(descriptor, "url");

        IFrameRenderStrategy dialogStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(plugin.getKey())
                .module(key)
                .dialogTemplate()
                .urlTemplate(url)
                .dialog(true)
                .dimensions("100%", "100%") // the client (js) will size the parent of the iframe
                .build();
        iFrameRenderStrategyRegistry.register(plugin.getKey(), key, DIALOG_CLASSIFIER,
                dialogStrategy);

        IFrameRenderStrategy simpleDialogStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(plugin.getKey())
                .module(key)
                .dialogTemplate()
                .urlTemplate(url)
                .simpleDialog(true)
                .dimensions("100%", "100%") // the client (js) will size the parent of the iframe
                .build();
        iFrameRenderStrategyRegistry.register(plugin.getKey(), key, SIMPLE_DIALOG_CLASSIFIER,
                simpleDialogStrategy);
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

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }
}
