package com.atlassian.labs.remoteapps.plugin.module.confluence;

import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.labs.remoteapps.plugin.module.RemoteModuleGenerator;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.*;
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
