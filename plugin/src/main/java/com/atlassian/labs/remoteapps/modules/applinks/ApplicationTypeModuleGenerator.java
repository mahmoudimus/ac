package com.atlassian.labs.remoteapps.modules.applinks;

import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.*;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

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
    public RemoteModule generate(RemoteAppCreationContext ctx, Element element)
    {
        return new RemoteModule()
        {
            @Override
            public Set<ModuleDescriptor> getModuleDescriptors()
            {
                return emptySet();
            }
        };
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
        Element e = descriptorElement.createCopy("dynamic-application-link");
        for (Element child : newArrayList((List<Element>) e.elements()))
        {
            String name = child.getName();
            if (!name.equals("oauth") && !name.equals("entity-type"))
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
