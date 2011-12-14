package com.atlassian.labs.remoteapps;

import com.atlassian.labs.remoteapps.descriptor.external.RemoteModuleDescriptor;
import com.atlassian.labs.remoteapps.installer.InstallationFailedException;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.ApplicationProperties;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.VisitorSupport;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.parseDocument;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.printDocument;
import static com.google.common.collect.Maps.newLinkedHashMap;

/**
 *
 */
@Component
public class DescriptorValidator
{
    private final ModuleGeneratorManager moduleGeneratorManager;
    private final ApplicationProperties applicationProperties;
    private final Plugin plugin;
    private final ProductAccessor productAccessor;

    @Autowired
    public DescriptorValidator(ModuleGeneratorManager moduleGeneratorManager,
                               ApplicationProperties applicationProperties,
                               PluginRetrievalService pluginRetrievalService, ProductAccessor productAccessor
    )
    {
        this.productAccessor = productAccessor;
        this.plugin = pluginRetrievalService.getPlugin();
        this.moduleGeneratorManager = moduleGeneratorManager;
        this.applicationProperties = applicationProperties;
    }

    public Document parseAndValidate(String url, String descriptorXml)
    {
        SAXReader reader = new SAXReader(true);
        try
        {
            InputSource source = new InputSource(new StringReader(descriptorXml));
            source.setSystemId(url);
            source.setEncoding("UTF-8");
            reader.setValidation(false);
            Document document = reader.read(source);
            document.accept(new NamespaceSetter(new Namespace("", getSchemaNamespace())));

            validate(document);
            document.accept(new NamespaceCleaner());
            return document;
        }
        catch (DocumentException e)
        {
            throw new InstallationFailedException("Unable to parse the descriptor: " + e.getMessage(), e);
        }
    }

    private void validate(Document doc)
    {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // load a WXS schema, represented by a Schema instance
        Source schemaFile = new StreamSource(new StringReader(buildSchema()));
        Schema schema = null;
        try
        {
            schema = factory.newSchema(schemaFile);
        }
        catch (SAXException e)
        {
            throw new RuntimeException("Unable to build schema", e);
        }

        // create a Validator instance, which can be used to validate an instance document
        Validator validator = schema.newValidator();

        // validate the DOM tree
        try
        {
            validator.validate(new DocumentSource(doc));
        }
        catch (SAXException e)
        {
            throw new InstallationFailedException("Unable to validate the descriptor: " + e.getMessage(), e);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Shouldn't happen", e);
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
        Map<String, Document> entities = newLinkedHashMap();

        final URL schemaUrl = getSchemaUrl();
        Document schema = parseDocument(schemaUrl);
        final Element root = schema.getRootElement();
        final Collection<Element> rootIncludes = root.elements("include");
        for (Element include : rootIncludes)
        {
            String schemaLocation = include.attributeValue("schemaLocation");
            final Document includeDoc = parseDocument(plugin.getResource("/xsd/" + schemaLocation));
            insertAvailableLinkContextParams(includeDoc, productAccessor.getLinkContextParams());
            entities.put(schemaLocation, includeDoc);
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
            for (Element child : ((Collection<Element>) importRoot.elements()))
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

    private void insertAvailableLinkContextParams(Document includeDoc, Map<String, String> linkContextParams)
    {
        Element restriction = (Element) includeDoc.selectSingleNode("/xs:schema/xs:simpleType[@name='LinkContextParamNameType']/xs:restriction");
        if (restriction != null)
        {
            for (Map.Entry<String, String> entry : linkContextParams.entrySet())
            {
                String name = entry.getKey();
                restriction.addElement("xs:enumeration").addAttribute("value", name);
            }
        }
    }

    private String getSchemaNamespace()
    {
        return applicationProperties.getBaseUrl() + "/rest/remoteapps/1/installer/schema/remote-app";
    }

    private static class NamespaceCleaner extends VisitorSupport
    {
        public void visit(Document document)
        {
            ((DefaultElement) document.getRootElement())
                    .setNamespace(Namespace.NO_NAMESPACE);
            document.getRootElement().additionalNamespaces().clear();
        }

        public void visit(Namespace namespace)
        {
            namespace.detach();
        }

        public void visit(Attribute node)
        {
            if (node.toString().contains("xmlns")
                    || node.toString().contains("xsi:"))
            {
                node.detach();
            }
        }

        public void visit(Element node)
        {
            if (node instanceof DefaultElement)
            {
                ((DefaultElement) node).setNamespace(Namespace.NO_NAMESPACE);
            }
        }

    }

    private static class NamespaceSetter extends VisitorSupport
    {
        private final Namespace namespace;

        public NamespaceSetter(Namespace namespace)
        {
            this.namespace = namespace;
        }

        public void visit(Document document)
        {
            ((DefaultElement) document.getRootElement())
                    .setNamespace(namespace);
        }

        public void visit(Element node)
        {
            if (node instanceof DefaultElement)
            {
                ((DefaultElement) node).setNamespace(namespace);
            }
        }

    }
}
