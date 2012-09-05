package com.atlassian.labs.remoteapps.plugin.module;

import com.atlassian.labs.remoteapps.plugin.product.ProductAccessor;
import com.atlassian.labs.remoteapps.spi.module.DynamicMarkerCondition;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;

import static com.atlassian.labs.remoteapps.plugin.module.util.redirect.RedirectServlet
        .getPermanentRedirectUrl;
import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.*;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Creates a builder for web item descriptor creation.  Builder instances
 * are meant to be shared across threads with instance-specific information passed
 * into the build() method
 */
@Component
public class WebItemCreator
{
    private final ProductAccessor productAccessor;
    private final ConditionProcessor conditionProcessor;
    private static final Logger log = LoggerFactory.getLogger(WebItemCreator.class);

    @Autowired
    public WebItemCreator(ProductAccessor productAccessor, ConditionProcessor conditionProcessor)
    {
        this.productAccessor = productAccessor;
        this.conditionProcessor = conditionProcessor;
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

        public WebItemModuleDescriptor build(Plugin plugin, String key, URI localUrl, Element configurationElement)
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
            config.addElement("label").setText(name);
            Element linkElement = config.addElement("link").
                    addAttribute("linkId", webItemKey);

            if (localUrl != null)
            {
                if (localUrl.toString().contains("$"))
                {
                    throw new PluginParseException("Invalid url '" + localUrl + "', cannot contain velocity expressions");
                }

                StringBuilder url = new StringBuilder();
                url.append("/plugins/servlet");
                url.append(localUrl);
                if (localUrl.getQuery() == null)
                {
                    url.append("?");
                }

                for (Map.Entry<String,String> entry : contextParams.entrySet())
                {
                    url.append(entry.getKey());
                    url.append("=");
                    url.append(entry.getValue());
                    url.append("&");
                }

                linkElement.setText(url.substring(0, url.length() - 1));
            }

            if (!StringUtils.isBlank(additionalStyleClass))
            {
                config.addElement("styleClass").
                        setText(additionalStyleClass);
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
            return createWebItemDescriptor(conditionProcessor.getLoadablePlugin(plugin), config);
        }



        private WebItemModuleDescriptor createWebItemDescriptor(
                Plugin plugin, Element config)
        {
            WebItemModuleDescriptor descriptor = productAccessor.createWebItemModuleDescriptor();
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
    }

}
