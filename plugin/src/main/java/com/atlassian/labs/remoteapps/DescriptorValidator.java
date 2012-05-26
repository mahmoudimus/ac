package com.atlassian.labs.remoteapps;

import com.atlassian.labs.remoteapps.api.InstallationFailedException;
import com.atlassian.labs.remoteapps.api.XmlUtils;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;
import org.dom4j.*;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
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
    private final WebResourceManager webResourceManager;

    @Autowired
    public DescriptorValidator(ModuleGeneratorManager moduleGeneratorManager,
                               ApplicationProperties applicationProperties,
                               PluginRetrievalService pluginRetrievalService, ProductAccessor productAccessor,
                               WebResourceManager webResourceManager
    )
    {
        this.productAccessor = productAccessor;
        this.webResourceManager = webResourceManager;
        this.plugin = pluginRetrievalService.getPlugin();
        this.moduleGeneratorManager = moduleGeneratorManager;
        this.applicationProperties = applicationProperties;
    }

    public Document parseAndValidate(String url, String descriptorXml)
    {
        SAXReader reader = XmlUtils.createSecureSaxReader();
        try
        {
            InputSource source = new InputSource(new StringReader(descriptorXml));
            source.setSystemId(url);
            source.setEncoding("UTF-8");
            Document document = reader.read(source);
            document.accept(new NamespaceCleaner());
            validate(url, document);
            return document;
        }
        catch (DocumentException e)
        {
            throw new InstallationFailedException("Unable to parse the descriptor: " + e.getMessage(), e);
        }
    }

    public void validate(String url, Document document)
    {
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        boolean useNamespace = document.getRootElement().getNamespaceURI().equals(getSchemaNamespace());
        StreamSource schemaSource = new StreamSource(new StringReader(
                buildSchema(getSchemaUrl(), useNamespace)));
        Schema schema;
        try
        {
            schema = schemaFactory.newSchema(schemaSource);
        }
        catch (SAXException e)
        {
            throw new RuntimeException("Couldn't parse built schema", e);
        }
        
        Validator validator = schema.newValidator();
        try
        {
            DocumentSource source = new DocumentSource(document);
            source.setSystemId(url);
            validator.validate(source);
        }
        catch (SAXException e)
        {
            throw new InstallationFailedException("Unable to parse the descriptor: " + e.getMessage(), e);
        }
        catch (IOException e)
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
        return buildSchema(getSchemaUrl(), false);
    }

    private String buildSchema(URL schemaUrl, boolean usesNamespace)
    {
        Set<String> includedDocIds = newHashSet();
        Element root = parseDocument(schemaUrl).getRootElement();

        // Add XSL stylesheet
        Map arguments = new HashMap();
        arguments.put( "type", "text/xsl" );
        arguments.put( "href", webResourceManager.getStaticPluginResource("com.atlassian.labs.remoteapps-plugin:schema-xsl",
                "xs3p.xsl", UrlMode.ABSOLUTE) );
        DocumentFactory factory = new DocumentFactory();
        ProcessingInstruction pi
                = factory.createProcessingInstruction( "xml-stylesheet", arguments );
        root.getDocument().content().add(0,pi);

        final String ns = getSchemaNamespace();
        if (usesNamespace)
        {
            root.addAttribute("targetNamespace", ns);
            root.addAttribute("xmlns", ns);
        }
        
        processIncludes(root.getDocument(), includedDocIds);
        Element modulesChoice = (Element) root.selectSingleNode("/xs:schema/xs:complexType[@name='RemoteAppType']/xs:choice");
        for (final RemoteModuleGenerator generator : this.moduleGeneratorManager.getAllValidatableGenerators())
        {
            final String id = generator.getSchema().getId();
            if (!includedDocIds.contains(id))
            {
                includedDocIds.add(id);
                Document doc = generator.getSchema().getDocument();
                processIncludes(doc, includedDocIds);
                for (Element child : (List<Element>)doc.getRootElement().elements())
                {
                    root.elements().add(0, child.detach());
                }
            }
            Element module = modulesChoice.addElement("xs:element")
                    .addAttribute("name", generator.getType())
                    .addAttribute("type", generator.getSchema().getComplexType())
                    .addAttribute("maxOccurs", generator.getSchema().getMaxOccurs());
            addSchemaDocumentation(module, generator);
        }

        return printNode(root.getDocument());
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