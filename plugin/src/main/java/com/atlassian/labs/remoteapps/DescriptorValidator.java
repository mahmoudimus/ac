package com.atlassian.labs.remoteapps;

import com.atlassian.labs.remoteapps.descriptor.external.RemoteModuleDescriptor;
import com.atlassian.labs.remoteapps.installer.InstallationFailedException;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.base.Function;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.*;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Sets.newHashSet;

/**
 *
 */
@Component
public class DescriptorValidator
{
    private final ModuleGeneratorManager moduleGeneratorManager;
    private final ApplicationProperties applicationProperties;
    private final Plugin plugin;

    @Autowired
    public DescriptorValidator(ModuleGeneratorManager moduleGeneratorManager,
                               ApplicationProperties applicationProperties,
                               PluginRetrievalService pluginRetrievalService
    )
    {
        this.plugin = pluginRetrievalService.getPlugin();
        this.moduleGeneratorManager = moduleGeneratorManager;
        this.applicationProperties = applicationProperties;
    }

    public Document parseAndValidate(String url, String descriptorXml)
    {
        SAXReader reader = new SAXReader(true);
        try
        {
            reader.setFeature("http://apache.org/xml/features/validation/schema", true);
            reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                    "http://www.w3.org/2001/XMLSchema");
            final InputSource schemaSource = new InputSource(new StringReader(buildSchema()));
            schemaSource.setSystemId(getSchemaUrl().toString());
            reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", schemaSource);

            InputSource source = new InputSource(new StringReader(descriptorXml));
            source.setSystemId(url);
            source.setEncoding("UTF-8"); 
            return reader.read(source);
        }
        catch (SAXException e)
        {
            throw new IllegalArgumentException(e);
        }
        catch (DocumentException e)
        {
            throw new InstallationFailedException("Unable to parse and validate the descriptor.  Ensure you have defined " +
                    "the correct namespace: " + getSchemaNamespace() + ". Error was: " + e.getMessage(), e);
        }
    }

    private URL getSchemaUrl()
    {
        return plugin.getResource("/xsd/remote-app.xsd");
    }


    public String getSchema()
    {
        return buildSchema();
    }

    private String buildSchema()
    {
        Map<String,Document> entities = newLinkedHashMap();

        final URL schemaUrl = getSchemaUrl();
        Document schema = parseDocument(schemaUrl);
        final Element root = schema.getRootElement();
        final Collection<Element> rootIncludes = root.elements("include");
        for (Element include : rootIncludes)
        {
            String schemaLocation = include.attributeValue("schemaLocation");
            entities.put(schemaLocation, parseDocument(plugin.getResource("/xsd/" + schemaLocation)));
        }
        rootIncludes.clear();

        for (final RemoteModuleDescriptor descriptor : this.moduleGeneratorManager.getDescriptors())
        {
            final String id = descriptor.getSchema().getId();
            if (!entities.containsKey(id))
            {
                entities.put(id, descriptor.getSchema().getDocument());
            }
        }


        final String ns = getSchemaNamespace();
        root.addAttribute("targetNamespace", ns);
        root.addAttribute("xmlns", ns);
        for (String id : entities.keySet())
        {
            Element importRoot = entities.get(id).getRootElement();
            for (Element child : ((Collection <Element>)importRoot.elements()))
            {
                // todo: handle new includes not caught previously
                if (!"include".equals(child.getName()))
                {
                    root.elements().add(0, child.detach());
                }
            }
        }

        Element choice = (Element) root.selectSingleNode("/xs:schema/xs:complexType[@name='RemoteAppType']/xs:choice");

        for (RemoteModuleDescriptor descriptor : moduleGeneratorManager.getDescriptors())
        {
            choice.addElement("xs:element")
                    .addAttribute("name", descriptor.getModule().getType())
                    .addAttribute("type", descriptor.getSchema().getComplexType())
                    .addAttribute("maxOccurs", descriptor.getSchema().getMaxOccurs());
        }

        return printDocument(schema);
    }

    private String getSchemaNamespace()
    {
        return applicationProperties.getBaseUrl() + "/rest/remoteapps/1/installer/schema/remote-app";
    }
}
