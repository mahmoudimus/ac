package com.atlassian.labs.remoteapps.descriptor;

import com.atlassian.labs.remoteapps.api.InstallationFailedException;
import com.atlassian.labs.remoteapps.modules.external.Schema;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.ProcessingInstruction;
import org.dom4j.io.DocumentSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.*;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.*;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Builds a schema and validates descriptors with it.  Supports remote app and plugin descriptors.
 */
@Component
public class DescriptorValidator
{
    private final Plugin plugin;
    private final ProductAccessor productAccessor;
    private final WebResourceManager webResourceManager;
    private final DescriptorValidatorProvider remoteAppDescriptorValidatorProvider;
    private final DescriptorValidatorProvider pluginDescriptorValidatorProvider;

    @Autowired
    public DescriptorValidator(PluginRetrievalService pluginRetrievalService,
                               ProductAccessor productAccessor,
                               WebResourceManager webResourceManager,
                               RemoteAppDescriptorValidatorProvider remoteAppDescriptorValidatorProvider,
                               PluginDescriptorValidatorProvider pluginDescriptorValidatorProvider
    )
    {
        this.productAccessor = productAccessor;
        this.webResourceManager = webResourceManager;
        this.plugin = pluginRetrievalService.getPlugin();
        this.remoteAppDescriptorValidatorProvider = remoteAppDescriptorValidatorProvider;
        this.pluginDescriptorValidatorProvider = pluginDescriptorValidatorProvider;
    }

    public void validate(URI url, Document document)
    {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        DescriptorValidatorProvider descriptorValidatorProvider = remoteAppDescriptorValidatorProvider;
        if (document.getRootElement().attribute("plugins-version") != null)
        {
            descriptorValidatorProvider = pluginDescriptorValidatorProvider;
        }

        boolean useNamespace = document.getRootElement().getNamespaceURI().equals(
                descriptorValidatorProvider.getSchemaNamespace());
        StreamSource schemaSource = new StreamSource(new StringReader(buildSchema(
                descriptorValidatorProvider, useNamespace)));
        javax.xml.validation.Schema schema;
        try
        {

            schema = schemaFactory.newSchema(schemaSource);
        }
        catch (SAXParseException e)
        {
            throw new RuntimeException("Couldn't parse built schema" + " on line " + e.getLineNumber() + " for file " + e.getPublicId(), e);
        }
        catch (SAXException e)
        {
            throw new RuntimeException("Couldn't parse built schema", e);
        }

        Validator validator = schema.newValidator();
        try
        {
            DocumentSource source = new DocumentSource(document);
            source.setSystemId(url.toString());
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

    public String getRemoteAppSchema()
    {
        return buildSchema(remoteAppDescriptorValidatorProvider, true);
    }

    public String getPluginSchema()
    {
        try
        {
            return buildSchema(pluginDescriptorValidatorProvider, true);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    private String buildSchema(DescriptorValidatorProvider descriptorValidatorProvider, boolean usesNamespace)
    {
        Set<String> includedDocIds = newHashSet();
        Element root = parseDocument(descriptorValidatorProvider.getSchemaUrl()).getRootElement();

        // Add XSL stylesheet
        Map arguments = new HashMap();
        arguments.put("type", "text/xsl");
        arguments.put("href",
                webResourceManager.getStaticPluginResource("com.atlassian.labs.remoteapps-plugin:schema-xsl",
                        "xs3p.xsl", UrlMode.ABSOLUTE));
        DocumentFactory factory = new DocumentFactory();
        ProcessingInstruction pi = factory.createProcessingInstruction("xml-stylesheet", arguments);
        root.getDocument().content().add(0, pi);

        final String ns = descriptorValidatorProvider.getSchemaNamespace();
        if (usesNamespace)
        {
            root.addAttribute("targetNamespace", ns);
            root.addAttribute("xmlns", ns);
        }

        processIncludes(root.getDocument(), includedDocIds);
        Element modulesChoice = (Element) root.selectSingleNode(
                "/xs:schema/xs:complexType[@name='" + descriptorValidatorProvider.getRootElementName() + "']//xs:choice");
        for (final Schema schema : descriptorValidatorProvider.getModuleSchemas())
        {
            final String id = schema.getFileName();
            if (!includedDocIds.contains(id))
            {
                includedDocIds.add(id);
                Document doc = schema.getDocument();
                checkNotNull(doc, "Document from generator " + schema.getFileName() + " is null");
                processIncludes(doc, includedDocIds);
                for (Element child : (List<Element>) doc.getRootElement().elements())
                {
                    root.elements().add(0, child.detach());
                }
            }
            Element module = modulesChoice.addElement("xs:element")
                                          .addAttribute("name", schema.getElementName())
                                          .addAttribute("type",
                                                  schema.getComplexType())
                                          .addAttribute("maxOccurs", schema.getMaxOccurs());
            addSchemaDocumentation(module, schema);
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
        Element restriction = (Element) includeDoc.selectSingleNode(
                "/xs:schema/xs:simpleType[@name='LinkContextParameterNameType']/xs:restriction");
        if (restriction != null)
        {
            for (Map.Entry<String, String> entry : linkContextParams.entrySet())
            {
                String name = entry.getKey();
                restriction.addElement("xs:enumeration").addAttribute("value", name);
            }
        }
    }
}