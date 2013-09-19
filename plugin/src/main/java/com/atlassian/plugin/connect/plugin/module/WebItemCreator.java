package com.atlassian.plugin.connect.plugin.module;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.module.webitem.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.DynamicMarkerCondition;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static com.atlassian.plugin.connect.plugin.module.util.redirect.RedirectServlet.getPermanentRedirectUrl;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getOptionalUriAttribute;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.printNode;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Creates a builder for web item descriptor creation.  Builder instances
 * are meant to be shared across threads with instance-specific information passed
 * into the build() method
 */
@Component
public final class WebItemCreator
{
    private final ConditionProcessor conditionProcessor;
    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;

    private static final Logger log = LoggerFactory.getLogger(WebItemCreator.class);

    @Autowired
    public WebItemCreator(ConditionProcessor conditionProcessor, WebItemModuleDescriptorFactory webItemModuleDescriptorFactory)
    {
        this.conditionProcessor = conditionProcessor;
        this.webItemModuleDescriptorFactory = webItemModuleDescriptorFactory;
    }

    public Builder newBuilder()
    {
        return new Builder();
    }

    public class Builder
    {
        private Class<? extends Condition> condition = AlwaysDisplayCondition.class;
        private String additionalStyleClass = "";
        private Map<String,String> contextParams;
        private int preferredWeight;
        private String preferredSectionKey;
        private boolean absolute;

        public WebItemModuleDescriptor build(Plugin plugin, String key, String webItemUrl, Element configurationElement)
        {
            notNull(condition);
            notNull(key);
            notNull(configurationElement);
            Element config = configurationElement.createCopy();
            config.elements("conditions").clear();
            final String webItemKey = "webitem-" + key;
            config.addAttribute("key", webItemKey);
            config.addAttribute("section",
                    getOptionalAttribute(configurationElement, "section", preferredSectionKey));
            config.addAttribute("weight", getOptionalAttribute(configurationElement, "weight", preferredWeight));

            String name = getOptionalAttribute(configurationElement, "link-name", escapeHtml(
                    getRequiredAttribute(configurationElement, "name")));
            config.addElement("label").addAttribute("key", name);
            Element linkElement = config.addElement("link").
                    addAttribute("linkId", key);

            String url = null;
            if (webItemUrl != null)
            {
                if (absolute)
                {
                    url = webItemUrl;
                }
                else
                {
                    UriBuilder uriBuilder = new UriBuilder(Uri.parse("/plugins/servlet" + webItemUrl));
                    String width = getOptionalAttribute(configurationElement, "width", null);
                    if (width != null) uriBuilder.addQueryParameter("width", width);
                    String height = getOptionalAttribute(configurationElement, "height", null);
                    if (height != null) uriBuilder.addQueryParameter("height", height);

                    url = uriBuilder.toString();
                    String sep = url.indexOf("?") > 0 ? "&" : "?";

                    for (Map.Entry<String,String> entry : contextParams.entrySet())
                    {
                        url += sep + entry.getKey() + "=" + entry.getValue();
                        sep = "&";
                    }
                }

                linkElement.setText(url);
            }

            if (!StringUtils.isBlank(additionalStyleClass))
            {
                config.addElement("styleClass").setText(additionalStyleClass);
            }
            convertIcon(plugin, configurationElement, config);
            config.addElement("condition")
                    .addAttribute("class", DynamicMarkerCondition.class.getName());
            if (condition != null)
            {
                config.addElement("condition").addAttribute("class", condition.getName());
            }


            Condition condition = conditionProcessor.process(configurationElement, config, plugin.getKey());
            if (condition instanceof ContainingRemoteCondition)
            {
                Element styleClass = config.element("styleClass");
                if (styleClass == null)
                {
                    styleClass = config.addElement("styleClass");
                }
                else
                {
                    styleClass.addText(" ");
                }
                styleClass.addText("remote-condition hidden " + conditionProcessor.createUniqueUrlHash(
                        plugin.getKey(), ((ContainingRemoteCondition) condition).getConditionUrl()));
            }

            if (log.isDebugEnabled())
            {
                log.debug("Created web item: " + printNode(config));
            }
            return createWebItemDescriptor(conditionProcessor.getLoadablePlugin(plugin), config, key, url);
        }

        private WebItemModuleDescriptor createWebItemDescriptor(Plugin plugin, Element config, String linkId, String url)
        {
            config.addAttribute("system", "true");
            final WebItemModuleDescriptor descriptor = webItemModuleDescriptorFactory.createWebItemModuleDescriptor(url, linkId, absolute);
            descriptor.init(plugin, config);
            return descriptor;
        }

        private void convertIcon(Plugin plugin, Element source, Element target)
        {
            URI iconUri = getOptionalUriAttribute(source, "icon-url");
            if (iconUri != null)
            {
                // todo: would be nice to detect the size or at least allow it to be configured
                target.addElement("icon")
                        .addAttribute("width", "16")
                        .addAttribute("height", "16")
                        .addElement("link")
                        .addText(getPermanentRedirectUrl(
                                plugin.getKey(), iconUri));
            }
        }

        public Builder setCondition(Class<? extends Condition> condition)
        {
            this.condition = condition;
            return this;
        }

        public Builder setAdditionalStyleClass(String additionalStyleClass)
        {
            this.additionalStyleClass = additionalStyleClass;
            return this;
        }

        public Builder setContextParams(Map<String, String> contextParams)
        {
            this.contextParams = contextParams;
            return this;
        }

        public Builder setContextParams(Set<String> contextParams)
        {
            ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
            for (String param : contextParams)
            {
                mapBuilder.put(param, String.format("${%s}", param));
            }
            return setContextParams(mapBuilder.build());
        }

        public Builder setPreferredWeight(int preferredWeight)
        {
            this.preferredWeight = preferredWeight;
            return this;
        }

        public Builder setPreferredSectionKey(String preferredSectionKey)
        {
            this.preferredSectionKey = preferredSectionKey;
            return this;
        }

        public Builder setAbsolute(boolean absolute)
        {
            this.absolute = absolute;
            return this;
        }
    }

}
