package com.atlassian.labs.remoteapps.modules;

import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.impl.AbstractDelegatingPlugin;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.util.Map;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
import static org.apache.commons.lang.Validate.notNull;

public class WebItemCreator
{
    private final ProductAccessor productAccessor;
    private final WebItemContext webItemContext;

    public WebItemCreator(WebItemContext webItemContext, ProductAccessor productAccessor)
    {
        this.webItemContext = webItemContext;
        this.productAccessor = productAccessor;
    }

    public WebItemModuleDescriptor createWebItemDescriptor(RemoteAppCreationContext ctx,
                                                           Element e,
                                                           String key,
                                                           String localUrl,
                                                           Condition condition, 
                                                           String additionalStyleClass)
    {
        notNull(condition);
        Element config = e.createCopy();
        final String webItemKey = "webitem-" + key;
        config.addAttribute("key", webItemKey);
        config.addAttribute("section",
                getOptionalAttribute(e, "section", webItemContext.getPreferredSectionKey()));
        config.addAttribute("weight", getOptionalAttribute(e, "weight", webItemContext.getPreferredWeight()));

        if (localUrl.contains("$"))
        {
            throw new PluginParseException("Invalid url '" + localUrl + "', cannot contain velocity expressions");
        }

        StringBuilder url = new StringBuilder();
        url.append("/plugins/servlet");
        url.append(localUrl);
        if (!localUrl.contains("?"))
        {
            url.append("?");
        }

        for (Map.Entry<String,String> entry : webItemContext.getContextParams().entrySet())
        {
            url.append(entry.getKey());
            url.append("=");
            url.append(entry.getValue());
        }
        String name = getRequiredAttribute(e, "name");
        config.addElement("label").setText(name);
        config.addElement("link").
                addAttribute("linkId", webItemKey).
                setText(url.toString());
        if (!StringUtils.isBlank(additionalStyleClass))
        {
            config.addElement("styleClass").
                    setText(additionalStyleClass);
        }
        config.addElement("condition")
                .addAttribute("class", DynamicMarkerCondition.class.getName());

        ConditionLoadingPlugin plugin = new ConditionLoadingPlugin(ctx.getPlugin(), condition);
        WebItemModuleDescriptor descriptor = productAccessor.createWebItemModuleDescriptor();
        descriptor.init(plugin, config);
        return descriptor;
    }

    private static class ConditionLoadingPlugin extends AbstractDelegatingPlugin
    {
        private final Condition condition;
        public ConditionLoadingPlugin(Plugin delegate, Condition condition)
        {
            super(delegate);
            this.condition = condition;
        }

        @Override
        public <T> Class<T> loadClass(String clazz, Class<?> callingClass) throws ClassNotFoundException
        {
            try
            {
                return super.loadClass(clazz, callingClass);
            }
            catch (ClassNotFoundException ex)
            {
                return (Class<T>) getClass().getClassLoader().loadClass(clazz);
            }
        }

        @Override
        public <T> T autowire(Class<T> clazz) throws UnsupportedOperationException
        {
            if (clazz == DynamicMarkerCondition.class)
            {
                return (T) condition;
            }

            return super.autowire(clazz);
        }

        @Override
        public <T> T autowire(Class<T> clazz,
                AutowireStrategy autowireStrategy) throws
                                                   UnsupportedOperationException
        {
            if (clazz == DynamicMarkerCondition.class)
            {
                return (T) condition;
            }

            return super.autowire(clazz,
                    autowireStrategy);
        }
    }
}
