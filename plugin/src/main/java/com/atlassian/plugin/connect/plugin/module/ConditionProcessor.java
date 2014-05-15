package com.atlassian.plugin.connect.plugin.module;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import com.atlassian.plugin.web.descriptors.ConditionElementParser;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.collect.Lists.transform;
import static org.apache.commons.lang.StringUtils.escape;
import static org.apache.commons.lang.StringUtils.join;

/**
 * Processes conditions, handling static and remote conditions
 */
@Component
public class ConditionProcessor
{
    private final ProductAccessor productAccessor;
    private final AutowireCapablePlugin remotablePlugin;

    @Autowired
    public ConditionProcessor(ProductAccessor productAccessor, PluginRetrievalService pluginRetrievalService)
    {
        this.productAccessor = productAccessor;
        this.remotablePlugin = (AutowireCapablePlugin) pluginRetrievalService.getPlugin();
    }

    public Condition process(Element oldConfig, Element newConfig, String pluginKey)
    {
        return process(oldConfig, newConfig, pluginKey, null);
    }

    public Condition process(Element oldConfig, Element newConfig, String pluginKey, String toHideSelector)
    {
        List<String> contextParamKeys = getContextParameters(oldConfig);
        newConfig.elements("conditions").clear();
        Element conditions = oldConfig.element("conditions");
        String remoteConditionUrl = null;

        if (conditions != null)
        {
            for (Element cElement : (List<Element>) conditions.elements("condition"))
            {
                String cName = cElement.attributeValue("name");
                String cUrl = cElement.attributeValue("url");
                if (cName != null && cUrl != null)
                {
                    throw new PluginParseException("Name and url cannot be defined on a condition");
                }
                else if (cName == null && cUrl == null)
                {
                    throw new PluginParseException("Either the name or url must be defined on a condition");
                }
                Element condElement = newConfig.addElement("condition");

                if (Boolean.parseBoolean(escape(cElement.attributeValue("invert"))))
                {
                    condElement.addAttribute("invert", "true");
                }

                if (cName != null)
                {
                    condElement.addAttribute("class", productAccessor.getConditions().get(cName).getName());
                    for (Object child : cElement.elements())
                    {
                        if (child instanceof Element)
                        {
                            Element el = (Element) child;
                            condElement.add(el.createCopy());
                        }
                    }
                }
                else
                {
                    remoteConditionUrl = cUrl;
                    if (toHideSelector == null)
                    {
                        String hash = createUniqueUrlHash(pluginKey, cUrl);
                        toHideSelector = "." + hash;
                    }
                    String paramList = contextParamKeys.isEmpty() ? "" : join(contextParamKeys, ",");
                    condElement.addAttribute("class", RemoteCondition.class.getName());
                    condElement.addElement("param").addAttribute("name", "pluginKey").addText(pluginKey).getParent()
                            .addElement("param").addAttribute("name", "url").addText(cUrl).getParent()
                            .addElement("param").addAttribute("name", "contextParams").addText(paramList).getParent()
                            .addElement("param").addAttribute("name", "toHideSelector").addText(
                            toHideSelector);
                }
            }
        }

        ConditionElementParser conditionElementParser = new ConditionElementParser(new ConditionElementParser.ConditionFactory()
        {
            @Override
            public Condition create(String className, Plugin plugin) throws
                    ConditionLoadingException
            {
                try
                {
                    return (Condition) remotablePlugin.autowire(((Plugin) remotablePlugin).loadClass
                                (className, getClass()));
                }
                catch (ClassNotFoundException e)
                {
                    throw new ConditionLoadingException(e);
                }
            }
        });
        if (newConfig.elements("condition").size() > 1)
        {
            Element root = newConfig.addElement("conditions").addAttribute("type", "AND");
            for (Element cond : (List<Element>) newConfig.elements("condition"))
            {
                root.add(cond.detach());
            }
        }
        Condition aggregateCondition = conditionElementParser.makeConditions((Plugin) remotablePlugin,
                newConfig,
                ConditionElementParser.CompositeType.AND);
        return remoteConditionUrl != null ? new ContainingRemoteCondition(aggregateCondition, remoteConditionUrl) :
                aggregateCondition;
    }

    public String createUniqueUrlHash(String pluginKey, String cUrl)
    {
        return "ap-hash-" + (cUrl + ":" + pluginKey).hashCode();
    }

    public Plugin getLoadablePlugin(Plugin plugin)
    {
        return new AutowireWithConnectPluginDecorator(remotablePlugin, plugin, Sets.<Class<?>>newHashSet(productAccessor.getConditions().values()));
    }

    private List<String> getContextParameters(Element oldConfig)
    {
        Element contextParameters = oldConfig.element("context-parameters");
        if (contextParameters != null)
        {
            return transform((List<Element>)contextParameters.elements("context-parameter"), new Function<Element, String>()

            {
                @Override
                public String apply(@Nullable Element input)
                {
                    return input.attributeValue("name");
                }
            } );
        }
        return Collections.emptyList();
    }
}
