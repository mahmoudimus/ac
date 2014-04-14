package com.atlassian.plugin.connect.plugin.module.webitem;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.plugin.integration.plugins.LegacyXmlDynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.ConditionProcessor;
import com.atlassian.plugin.connect.plugin.module.WebItemCreator;
import com.atlassian.plugin.connect.plugin.module.page.RemotePageDescriptorCreator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import java.net.URI;

import static com.atlassian.plugin.connect.plugin.module.page.RemotePageDescriptorCreator.createLocalUrl;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

public class RemoteWebItemModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final RemotePageDescriptorCreator remotePageDescriptorCreator;
    private final WebItemCreator webItemCreator;
    private final UrlValidator urlValidator;
    private final ConditionProcessor conditionProcessor;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final RemotablePluginAccessorFactory pluginAccessorFactory;

    private Element descriptor;
    private Element link;
    private LegacyXmlDynamicDescriptorRegistration.Registration registration;
    private String url;
    private String moduleKey;

    public RemoteWebItemModuleDescriptor(
            ModuleFactory moduleFactory,
            LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration,
            RemotePageDescriptorCreator remotePageDescriptorCreator,
            UrlValidator urlValidator,
            ConditionProcessor conditionProcessor,
            WebItemCreator webItemCreator,
            UrlVariableSubstitutor urlVariableSubstitutor,
            RemotablePluginAccessorFactory pluginAccessorFactory)
    {
        super(moduleFactory);
        this.pluginAccessorFactory = checkNotNull(pluginAccessorFactory);
        this.urlVariableSubstitutor = checkNotNull(urlVariableSubstitutor);
        this.urlValidator = checkNotNull(urlValidator);
        this.webItemCreator = checkNotNull(webItemCreator);
        this.conditionProcessor = checkNotNull(conditionProcessor);
        this.dynamicDescriptorRegistration = checkNotNull(dynamicDescriptorRegistration);
        this.remotePageDescriptorCreator = checkNotNull(remotePageDescriptorCreator);
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.descriptor = element;
        this.link = element.element("link");
        this.moduleKey = getRequiredAttribute(descriptor, "key");
        this.url = link.getText();
        urlValidator.validate(url);
    }

    @Override
    public void enabled()
    {
        super.enabled();
        Element desc = descriptor.createCopy();

        final WebItemCreator.Builder webItemBuilder = webItemCreator.newBuilder();
        webItemBuilder.setContextParams(urlVariableSubstitutor.getContextVariableMap(url));
        createIcon(desc);

        if (isAbsolute())
        {
            webItemBuilder.setAbsolute(true);
            DescriptorToRegister webItemModuleDescriptor = new DescriptorToRegister(webItemBuilder.build(plugin, moduleKey, url, desc));
            dynamicDescriptorRegistration.registerDescriptors(conditionProcessor.getLoadablePlugin(getPlugin()), webItemModuleDescriptor);
        }
        else
        {
            String localUrl = createLocalUrl(plugin.getKey(), moduleKey);
            RemotePageDescriptorCreator.Builder containerPageBuilder = remotePageDescriptorCreator.newBuilder();
            decorateWebItem(desc, containerPageBuilder);

            DescriptorToRegister servletDescriptor = containerPageBuilder.createServletDescriptor(plugin, desc, moduleKey, url, localUrl, webItemBuilder.getContextParams());
            DescriptorToRegister webItemModuleDescriptor = new DescriptorToRegister(webItemBuilder.build(plugin, moduleKey, localUrl, desc));

            this.registration = dynamicDescriptorRegistration.registerDescriptors(
                    conditionProcessor.getLoadablePlugin(getPlugin()), servletDescriptor, webItemModuleDescriptor);
        }
    }

    private void createIcon(final Element descriptor)
    {
        Element iconElement = descriptor.element("icon");
        if (iconElement != null)
        {
            Element iconLinkElement = iconElement.element("link");
            if (iconLinkElement != null)
            {
                URI iconPath = URI.create(iconLinkElement.getText());
                iconLinkElement.setText(pluginAccessorFactory.get(plugin.getKey()).getTargetUrl(iconPath).toString());
            }
            else
            {
                throw new PluginParseException("Icon link element must be specified.");
            }
        }
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
    public Void getModule()
    {
        return null;
    }

    private void decorateWebItem(Element descriptor, RemotePageDescriptorCreator.Builder builder)
    {
        String type = getOptionalAttribute(descriptor, "type", "page");
        if ("page".equals(type))
        {
            builder.setDecorator("atl.general");
        }
        else if ("dialog".equals(type))
        {
            builder.setTemplateSuffix("-dialog").setWebItemStyleClass("ap-dialog");
        }
    }

    private boolean isAbsolute()
    {
        boolean absolute = Boolean.parseBoolean(getOptionalAttribute(link, "absolute", "false"));
        return absolute || url.startsWith("http");
    }

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }

}
