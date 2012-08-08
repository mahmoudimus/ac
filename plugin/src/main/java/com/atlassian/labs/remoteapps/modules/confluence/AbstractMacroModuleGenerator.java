package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.external.RemoteModule;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.*;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Parent for all macro types
 */
public abstract class AbstractMacroModuleGenerator implements RemoteModuleGenerator
{
    private final PluginAccessor pluginAccessor;

    public AbstractMacroModuleGenerator(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        Map<String,String> i18n = newHashMap();
        String macroKey = getRequiredAttribute(element, "key");
        if (element.element("parameters") != null)
        {
            for (Element parameter : new ArrayList<Element>(element.element("parameters").elements("parameter")))
            {
                String paramTitle = getRequiredAttribute(parameter, "title");
                String paramName = getRequiredAttribute(parameter, "name");
                if (paramTitle != null)
                {
                    i18n.put(pluginKey + "." + macroKey + ".param." + paramName + ".label", paramTitle);
                }

                String description = parameter.elementText("description");
                if (!StringUtils.isBlank(description))
                {
                    i18n.put(pluginKey + "." + macroKey + ".param." + paramName + ".desc", description);
                }
            }
        }
        String macroName = getOptionalAttribute(element, "title", getOptionalAttribute(element, "name", macroKey));
        i18n.put(pluginKey + "." + macroKey + ".label", macroName);

        if (element.element("description") != null)
        {
            i18n.put(pluginKey + "." + macroKey + ".desc", element.element("description").getTextTrim());
        }

        return i18n;
    }

    @Override
    public RemoteModule generate(final RemoteAppCreationContext ctx, Element entity)
    {
        return RemoteModule.NO_OP;
    }

    @Override
    public void validate(Element element, URI registrationUrl, String username) throws
                                                                                   PluginParseException
    {
        Element root = element.getParent();

        String key = getRequiredAttribute(element, "key");
        String appKey = root.attributeValue("key");

        for (XhtmlMacroModuleDescriptor descriptor : pluginAccessor.getEnabledModuleDescriptorsByClass(XhtmlMacroModuleDescriptor.class))
        {
            if (key.equals(descriptor.getKey()) && !appKey.equals(descriptor.getPluginKey()))
            {
                throw new PluginParseException("Macro key '" + key + "' already used by app '" + descriptor.getPluginKey() + "'");
            }
        }
        getRequiredUriAttribute(element, "url");
    }

}
