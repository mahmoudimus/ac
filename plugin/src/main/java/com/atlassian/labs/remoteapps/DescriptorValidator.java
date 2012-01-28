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
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.addSchemaDocumentation;
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
            reader.setFeature("http://apache.org/xml/features/validation/schema", true);
            reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                    "http://www.w3.org/2001/XMLSchema");
            boolean useNamespace = descriptorXml.contains(getSchemaNamespace());
            final InputSource schemaSource = new InputSource(new StringReader(buildSchema(useNamespace)));
            schemaSource.setSystemId(getSchemaUrl().toString());
            reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", schemaSource);

            InputSource source = new InputSource(new StringReader(descriptorXml));
            source.setSystemId(url);
            source.setEncoding("UTF-8");
            Document document = reader.read(source);

            document.accept(new NamespaceCleaner());
            return document;
        }
        catch (DocumentException e)
        {
            throw new InstallationFailedException("Unable to parse the descriptor: " + e.getMessage(), e);
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }

    private URL getSchemaUrl()
    {
        return plugin.getResource("/xsd/remote-app.xsd");
    }


    public String getSchema()
    {
        return buildSchema(false);
    }

    private String buildSchema(boolean usesNamespace)
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
        if (usesNamespace)
        {
            root.addAttribute("targetNamespace", ns);
            root.addAttribute("xmlns", ns);
        }

        for (String id : entities.keySet())
        {
            Element importRoot = entities.get(id).getRootElement();
            for (Element child : ((Collection<Element>) importRoot.elements()))
            {
                if ("include".equals(child.getName()))
                {
                    String schemaLocation = child.attributeValue("schemaLocation");
                    if (!entities.containsKey(schemaLocation))
                    {
                        throw new RuntimeException("Unreferenced common schema: " + schemaLocation);
                    }
                }
                else
                {
                    root.elements().add(0, child.detach());
                }
            }
        }

        Element choice = (Element) root.selectSingleNode("/xs:schema/xs:complexType[@name='RemoteAppType']/xs:choice");

        for (RemoteModuleDescriptor descriptor : moduleGeneratorManager.getDescriptors())
        {
            Element module = choice.addElement("xs:element")
                    .addAttribute("name", descriptor.getModule().getType())
                    .addAttribute("type", descriptor.getSchema().getComplexType())
                    .addAttribute("maxOccurs", descriptor.getSchema().getMaxOccurs());
            addSchemaDocumentation(module, descriptor);
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
