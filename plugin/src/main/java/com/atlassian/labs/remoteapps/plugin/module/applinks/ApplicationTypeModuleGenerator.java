package com.atlassian.labs.remoteapps.plugin.module.applinks;

import com.atlassian.labs.remoteapps.plugin.module.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.spi.schema.Schema;
import com.atlassian.plugin.PluginParseException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.*;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyMap;

/**
 * Generates application-type modules
 */
@Component
public class ApplicationTypeModuleGenerator implements RemoteModuleGenerator
{
    private static final Logger log = LoggerFactory.getLogger(ApplicationTypeModuleGenerator.class);

    @Override
    public Schema getSchema()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDescription()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType()
    {
        return null;
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
        Element e = descriptorElement.createCopy("remote-plugin-container");
        e.addAttribute("key", descriptorElement.attributeValue("key") + "-applink");
        for (Element child : newArrayList((List<Element>) e.elements()))
        {
            String name = child.getName();
            if (!name.equals("oauth"))
            {
                e.remove(child);
            }
        }

        pluginDescriptorRoot.add(e);
    }

    @Override
    public void validate(Element root, URI registrationUrl, String username)
    {
        URI displayUrl = getOptionalUriAttribute(root, "display-url");
        if (displayUrl == null || !registrationUrl.toString().startsWith(displayUrl.toString()))
        {
            throw new PluginParseException("display-url '" + displayUrl + "' must exist and match registration URL");
        }

    }
}
