package com.atlassian.labs.remoteapps;

import com.atlassian.labs.remoteapps.descriptor.external.RemoteModuleDescriptor;
import com.atlassian.labs.remoteapps.installer.InstallationFailedException;
import com.google.common.base.Function;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
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
import java.util.Map;
import java.util.concurrent.Callable;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.printInputSource;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.transformDocument;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;

/**
 *
 */
@Component
public class DescriptorValidator
{
    private final ModuleGeneratorManager moduleGeneratorManager;

    @Autowired
    public DescriptorValidator(ModuleGeneratorManager moduleGeneratorManager)
    {
        this.moduleGeneratorManager = moduleGeneratorManager;
    }

    public Document parseAndValidate(String url, String descriptorXml)
    {
        SAXReader reader = new SAXReader(true);
        try
        {
            final MyEntityResolver entityResolver = new MyEntityResolver(moduleGeneratorManager);
            reader.setFeature("http://apache.org/xml/features/validation/schema", true);
            reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                    "http://www.w3.org/2001/XMLSchema");
            reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", new InputSource(new StringReader(buildSchema(entityResolver))));

            reader.setEntityResolver(entityResolver);
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
            throw new IllegalArgumentException(e);
        }
    }


    public String getSchema()
    {
        return buildSchema(new MyEntityResolver(moduleGeneratorManager));
    }

    private String buildSchema(final MyEntityResolver entityResolver)
    {
        return transformDocument(getClass().getResource("/xsd/remote-app.xsd"), new Function<Document,Document>()
        {
            @Override
            public Document apply(Document schema)
            {
                final Element root = schema.getRootElement();
                for (String id : entityResolver.getSchemaIds())
                {
                    root.content().add(0, root.addElement("xs:include").addAttribute("schemaLocation", id).detach());
                }

                Element choice = (Element) root.selectSingleNode("/xs:schema/xs:complexType[@name='RemoteAppType']/xs:choice");

                for (RemoteModuleDescriptor descriptor : moduleGeneratorManager.getDescriptors())
                {
                    choice.addElement("xs:element")
                            .addAttribute("name", descriptor.getModule().getType())
                            .addAttribute("type", descriptor.getSchema().getComplexType())
                            .addAttribute("maxOccurs", descriptor.getSchema().getMaxOccurs());
                }
                return schema;
            }
        });
    }

    public String getSchemaInclude(String includeFile)
    {
        for (RemoteModuleDescriptor descriptor : this.moduleGeneratorManager.getDescriptors())
        {
            if (includeFile.equals(descriptor.getSchema().getId()))
            {
                try
                {
                    return printInputSource(descriptor.getSchema().getInputSource());
                }
                catch (IOException e)
                {
                    throw new IllegalArgumentException("Unable to read include file: " + includeFile);
                }
            }
        }
        return null;
    }

    private static class MyEntityResolver implements EntityResolver
    {
        private final ModuleGeneratorManager moduleGeneratorManager;
        private final Map<String, InputSource> entities;

        public MyEntityResolver(ModuleGeneratorManager moduleGeneratorManager)
        {
            this.moduleGeneratorManager = moduleGeneratorManager;
            entities = newHashMap();
            for (final RemoteModuleDescriptor descriptor : this.moduleGeneratorManager.getDescriptors())
            {
                entities.put(descriptor.getSchema().getId(), descriptor.getSchema().getInputSource());
            }
            entities.put("common.xsd", new InputSource(getClass().getResource("/xsd/common.xsd").toString()));
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
        {
            String id = systemId.substring(systemId.lastIndexOf("/") + 1);
            return entities.get(id);
        }

        public Iterable<String> getSchemaIds()
        {
            return entities.keySet();
        }

    }
}
