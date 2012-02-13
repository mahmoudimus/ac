package com.atlassian.labs.remoteapps;

import com.atlassian.labs.remoteapps.descriptor.external.RemoteModuleDescriptor;
import com.atlassian.labs.remoteapps.installer.InstallationFailedException;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.ApplicationProperties;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.net.URL;
import java.util.*;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.*;
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
        Set<String> includedDocIds = newHashSet();
        Element root = parseDocument(getSchemaUrl()).getRootElement();
        final String ns = getSchemaNamespace();
        if (usesNamespace)
        {
            root.addAttribute("targetNamespace", ns);
            root.addAttribute("xmlns", ns);
        }
        
        processIncludes(root.getDocument(), includedDocIds);
        Element modulesChoice = (Element) root.selectSingleNode("/xs:schema/xs:complexType[@name='RemoteAppType']/xs:choice");
        for (final RemoteModuleDescriptor descriptor : this.moduleGeneratorManager.getDescriptors())
        {
            final String id = descriptor.getSchema().getId();
            if (!includedDocIds.contains(id))
            {
                includedDocIds.add(id);
                Document doc = descriptor.getSchema().getDocument();
                processIncludes(doc, includedDocIds);
                for (Element child : (List<Element>)doc.getRootElement().elements())
                {
                    root.elements().add(0, child.detach());
                }
            }
            Element module = modulesChoice.addElement("xs:element")
                    .addAttribute("name", descriptor.getModule().getType())
                    .addAttribute("type", descriptor.getSchema().getComplexType())
                    .addAttribute("maxOccurs", descriptor.getSchema().getMaxOccurs());
            addSchemaDocumentation(module, descriptor);
        }

        return printDocument(root.getDocument());
    }

    private void processIncludes(Document doc, Set<String> includedDocIds)
    {
        Element root = doc.getRootElement();
        final Collection<Element> rootIncludes = root.elements("include");
        for (Element include : rootIncludes)
        {
            int pos = include.getParent().elements().indexOf(include);
            include.detach();
            String schemaLocation = include.attributeValue("schemaLocation");
            if (!includedDocIds.contains(schemaLocation))
            {
                final Document includeDoc = parseDocument(plugin.getResource("/xsd/" + schemaLocation));
                processIncludes(includeDoc, includedDocIds);
                if (schemaLocation.equals("common.xsd"))
                {
                    insertAvailableLinkContextParams(includeDoc, productAccessor.getLinkContextParams());
                }
                List<Element> includeChildren = (List<Element>) includeDoc.getRootElement().elements();
                Collections.reverse(includeChildren);
                for (Element child : includeChildren)
                {
                    if (!root.elements().isEmpty())
                    {
                        root.elements().add(pos, child.detach());
                    }
                    else
                    {
                        root.add(child.detach());
                    }
                }
                includedDocIds.add(schemaLocation);
            }
        }
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
}