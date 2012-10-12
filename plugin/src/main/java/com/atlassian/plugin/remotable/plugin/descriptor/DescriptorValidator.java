package com.atlassian.plugin.remotable.plugin.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.remotable.plugin.PermissionManager;
import com.atlassian.plugin.remotable.plugin.product.ProductAccessor;
import com.atlassian.plugin.remotable.spi.InstallationFailedException;
import com.atlassian.plugin.remotable.spi.permission.Permission;
import com.atlassian.plugin.remotable.spi.permission.scope.ApiResourceInfo;
import com.atlassian.plugin.remotable.spi.permission.scope.ApiScope;
import com.atlassian.plugin.schema.spi.Schema;
import com.atlassian.plugin.web.Condition;
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
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.*;
import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Sets.*;

/**
 * Builds a schema and validates descriptors with it.  Supports remote plugin and plugin descriptors.
 */
@Component
public class DescriptorValidator
{
    private final Plugin plugin;
    private final ProductAccessor productAccessor;
    private final WebResourceManager webResourceManager;
    private final PermissionManager permissionManager;
    private final DescriptorValidatorProvider pluginDescriptorValidatorProvider;

    @Autowired
    public DescriptorValidator(PluginRetrievalService pluginRetrievalService,
                               ProductAccessor productAccessor,
                               WebResourceManager webResourceManager,
                               PermissionManager permissionManager,
                               PluginDescriptorValidatorProvider pluginDescriptorValidatorProvider
    )
    {
        this.productAccessor = productAccessor;
        this.webResourceManager = webResourceManager;
        this.permissionManager = permissionManager;
        this.plugin = pluginRetrievalService.getPlugin();
        this.pluginDescriptorValidatorProvider = pluginDescriptorValidatorProvider;
    }

    public void validate(URI url, Document document)
    {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        DescriptorValidatorProvider descriptorValidatorProvider = pluginDescriptorValidatorProvider;

        boolean useNamespace = document.getRootElement().getNamespaceURI().equals(
                descriptorValidatorProvider.getSchemaNamespace());
        StreamSource schemaSource = new StreamSource(new StringReader(buildSchema(descriptorValidatorProvider, useNamespace)));
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

        descriptorValidatorProvider.performSecondaryValidations(document);
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

        Element permissionsType = (Element) root.selectSingleNode(
                        "/xs:schema/xs:simpleType[@name='PermissionValueType']/xs:restriction");
        for (Permission permission : permissionManager.getPermissions())
        {
            Element enumeration = permissionsType.addElement("xs:enumeration").addAttribute("value", permission.getKey());
            Element doc = addSchemaDocumentation(enumeration, permission);

            if (permission instanceof ApiScope)
            {
                ApiScope apiScope = (ApiScope) permission;
                Element resources = doc.addElement("resources");
                for (ApiResourceInfo resource : apiScope.getApiResourceInfos())
                {

                    Element res = resources.addElement("resource").
                            addAttribute("path", resource.getPath()).
                            addAttribute("httpMethod", resource.getHttpMethod());
                    if (resource.getRpcMethod() != null)
                    {
                        res.addAttribute("rpcMethod", resource.getRpcMethod());
                    }
                }
            }
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
                final URL resource = plugin.getResource("/xsd/" + schemaLocation);
                if (resource == null)
                {
                    throw new IllegalArgumentException("Can't find resource: " + schemaLocation);
                }
                final Document includeDoc = parseDocument(resource);
                processIncludes(includeDoc, includedDocIds);
                if (schemaLocation.equals("common.xsd"))
                {
                    insertAvailableLinkContextParams(includeDoc, productAccessor.getLinkContextParams());
                    insertAvailableWebConditions(includeDoc, productAccessor.getConditions());
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

    private void insertAvailableWebConditions(Document includeDoc, Map<String, Class<? extends Condition>> webConditions)
    {
        Element restriction = (Element) includeDoc.selectSingleNode(
                "/xs:schema/xs:simpleType[@name='ConditionNameType']/xs:restriction");
        if (restriction != null)
        {
            for (String name : webConditions.keySet())
            {
                restriction.addElement("xs:enumeration").addAttribute("value", name);
            }
        }
    }
}