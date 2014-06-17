package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.PermissionManager;
import com.atlassian.plugin.connect.spi.InstallationFailedException;
import com.atlassian.plugin.connect.spi.permission.Permission;
import com.atlassian.plugin.connect.spi.permission.scope.ApiResourceInfo;
import com.atlassian.plugin.connect.spi.permission.scope.ApiScope;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.connect.spi.util.Dom4jUtils;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.schema.spi.Schema;
import com.atlassian.plugin.schema.spi.SchemaDocumented;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.DocumentSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.*;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.parseDocument;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.printNode;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Builds a schema and validates descriptors with it.  Supports remote plugin and plugin descriptors.
 */
@Component
@XmlDescriptor
public final class DescriptorValidator
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
                               PluginDescriptorValidatorProvider pluginDescriptorValidatorProvider)
    {
        this.productAccessor = productAccessor;
        this.webResourceManager = webResourceManager;
        this.permissionManager = permissionManager;
        this.plugin = pluginRetrievalService.getPlugin();
        this.pluginDescriptorValidatorProvider = pluginDescriptorValidatorProvider;
    }

    public void validate(URI url, Document document)
    {
        final boolean useNamespace = document.getRootElement().getNamespaceURI().equals(pluginDescriptorValidatorProvider.getSchemaNamespace());

        final String builtSchema = buildSchema(pluginDescriptorValidatorProvider, useNamespace);
        try
        {
            javax.xml.validation.Schema schema = getSchema(CharStreams.newReaderSupplier(builtSchema), new PluginLSResourceResolver(plugin));
            Validator validator = schema.newValidator();
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

        pluginDescriptorValidatorProvider.performSecondaryValidations(document);
    }

    static javax.xml.validation.Schema getSchema(InputSupplier<? extends Reader> schemaInput, LSResourceResolver resourceResolver) throws IOException
    {
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Reader schemaReader = null;
        try
        {
            schemaReader = schemaInput.getInput();
            schemaFactory.setResourceResolver(resourceResolver);
            return schemaFactory.newSchema(new StreamSource(schemaReader));
        }
        catch (SAXParseException e)
        {
            throw new RuntimeException(String.format("Couldn't parse schema (line %s, column %s):\n%s",
                    e.getLineNumber(), e.getColumnNumber(), CharStreams.toString(schemaInput)),
                    e);
        }
        catch (SAXException e)
        {
            throw new RuntimeException("Couldn't parse schema:\n" + CharStreams.toString(schemaInput), e);
        }
        finally
        {
            Closeables.closeQuietly(schemaReader);
        }
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
        return buildSchema(parseDocument(descriptorValidatorProvider.getSchemaUrl()), descriptorValidatorProvider, usesNamespace);
    }

    private String buildSchema(Document schema, DescriptorValidatorProvider descriptorValidatorProvider, boolean usesNamespace)
    {
        addNamespace(schema, descriptorValidatorProvider, usesNamespace);

        final Set<String> includedDocIds = newHashSet();

        processIncludes(schema, includedDocIds);

        addModules(schema, descriptorValidatorProvider, includedDocIds);

        addPermissions(schema, permissionManager.getPermissions());

        addXslStyleSheet(schema);

        return printNode(schema);
    }

    private void addModules(Document schema, DescriptorValidatorProvider descriptorValidatorProvider, Set<String> includedDocIds)
    {
        final Element choiceOfModules = selectSingleNode(schema, "/xs:schema/xs:complexType[@name='%s']//xs:choice", descriptorValidatorProvider.getRootElementName());
        for (final Schema moduleSchema : descriptorValidatorProvider.getModuleSchemas())
        {
            addModule(choiceOfModules, moduleSchema, includedDocIds);
        }
    }

    private void addModule(Element choiceOfModules, Schema moduleSchema, Set<String> includedDocIds)
    {
        final String id = moduleSchema.getFileName();

        if (!includedDocIds.contains(id))
        {
            includedDocIds.add(id);

            final Document moduleSchemaDocument = moduleSchema.getDocument();
            checkNotNull(moduleSchemaDocument, "Document from generator " + moduleSchema.getFileName() + " is null");
            processIncludes(moduleSchemaDocument, includedDocIds);
            addModuleSchemaElementsToSchema(choiceOfModules, moduleSchemaDocument);
        }

        final Element module = addModuleElementToSchema(choiceOfModules, moduleSchema);
        final Element moduleDocumentation = addSchemaDocumentation(module, moduleSchema);
        addPermissionDocumentation(moduleDocumentation, moduleSchema);
    }

    @VisibleForTesting
    static void addPermissionDocumentation(Element moduleDocumentation, Schema moduleSchema)
    {
        addPermissionDocumentation(moduleDocumentation, "required-permissions", moduleSchema.getRequiredPermissions());
        addPermissionDocumentation(moduleDocumentation, "optional-permissions", moduleSchema.getOptionalPermissions());
    }

    private static void addPermissionDocumentation(Element moduleDocumentation, String permissionsElementName, Iterable<String> permissions)
    {
        if (Iterables.isEmpty(permissions))
        {
            return;
        }

        final Element permissionsElement = moduleDocumentation.addElement(permissionsElementName);
        for (String permission : permissions)
        {
            permissionsElement.addElement("permission").setText(permission);
        }
    }

    private void addModuleSchemaElementsToSchema(Element choiceOfModules, Document doc)
    {
        for (Element child : (List<Element>) doc.getRootElement().elements())
        {
            choiceOfModules.getDocument().getRootElement().elements().add(0, child.detach());
        }
    }

    private Element addModuleElementToSchema(Element choiceOfModules, Schema moduleSchema)
    {
        return choiceOfModules.addElement("xs:element")
                .addAttribute("name", moduleSchema.getElementName())
                .addAttribute("type", moduleSchema.getComplexType())
                .addAttribute("maxOccurs", moduleSchema.getMaxOccurs());
    }

    private static Element selectSingleNode(final Node node, String xpath, String... args)
    {
        final String actualXpath = String.format(xpath, args);
        final Element element = (Element) node.selectSingleNode(actualXpath);
        checkState(element != null, "Could not find single node for xpath '%s' in:\n%s\n", actualXpath, LazyToString.of(new Supplier<String>()
        {
            @Override
            public String get()
            {
                return Dom4jUtils.printNode(node);
            }
        }));
        return element;
    }

    private static void addPermissions(Document schema, Iterable<Permission> permissions)
    {
        final Element permissionsType = selectSingleNode(schema, "/xs:schema/xs:simpleType[@name='PermissionValueType']/xs:restriction");
        for (Permission permission : permissions)
        {
            addPermission(permissionsType, permission);
        }
    }

    private static void addPermission(Element permissionsType, Permission permission)
    {
        final Element enumeration = permissionsType.addElement("xs:enumeration").addAttribute("value", permission.getKey());
        final Element doc = addSchemaDocumentation(enumeration, permission);
        if (permission instanceof ApiScope)
        {
            addApiScopeResourcesInformation(doc, (ApiScope) permission);
        }
    }

    private static void addApiScopeResourcesInformation(Element doc, ApiScope apiScope)
    {
        final Element resources = doc.addElement("resources");
        for (ApiResourceInfo resource : apiScope.getApiResourceInfos())
        {
            addApiScopeResourceInformation(resources, resource);
        }
    }

    private static void addApiScopeResourceInformation(Element resources, ApiResourceInfo resource)
    {
        final Element res = resources.addElement("resource").
                addAttribute("path", resource.getPath()).
                addAttribute("httpMethod", resource.getHttpMethod());
        if (resource.getRpcMethod() != null)
        {
            res.addAttribute("rpcMethod", resource.getRpcMethod());
        }
    }

    public static Element addSchemaDocumentation(Element source, SchemaDocumented generator)
    {
        final Element doc = getOrAddElementsIfDoNotExist(source, "xs:annotation", "xs:documentation");

        final Element name = getOrAddElementIfDoesNotExist(doc, "name");
        if (isBlank(name.getText()) && generator.getName() != null)
        {
            name.setText(generator.getName());
        }

        final Element desc = getOrAddElementIfDoesNotExist(doc, "description");
        if (isBlank(desc.getText()) && generator.getDescription() != null)
        {
            desc.setText(generator.getDescription());
        }
        return doc;
    }

    private static Element getOrAddElementsIfDoNotExist(Element source, String... names)
    {
        Element el = source;
        for (String name : names)
        {
            el = getOrAddElementIfDoesNotExist(el, name);
        }
        return el;
    }

    private static Element getOrAddElementIfDoesNotExist(Element source, String name)
    {
        Element el = source.element(name);
        if (el == null)
        {
            el = source.addElement(name);
        }
        return el;
    }

    private static void addNamespace(Document schema, DescriptorValidatorProvider descriptorValidatorProvider, boolean usesNamespace)
    {
        if (usesNamespace)
        {
            final String ns = descriptorValidatorProvider.getSchemaNamespace();
            final Element root = schema.getRootElement();
            root.addAttribute("targetNamespace", ns);
            root.addAttribute("xmlns", ns);
        }
    }

    private void addXslStyleSheet(Document schema)
    {
        final Map<String, String> arguments = newHashMap();
        arguments.put("type", "text/xsl");
        arguments.put("href", getXslStyleSheetUrl());

        schema.content().add(0, new DocumentFactory().createProcessingInstruction("xml-stylesheet", arguments));
    }

    private String getXslStyleSheetUrl()
    {
        return webResourceManager.getStaticPluginResource(ConnectPluginInfo.getPluginKey() + ":schema-xsl", "xs3p.xsl", UrlMode.ABSOLUTE);
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
        Element restriction = (Element) includeDoc.selectSingleNode("/xs:schema/xs:simpleType[@name='LinkContextParameterNameType']/xs:restriction");
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
        Element restriction = (Element) includeDoc.selectSingleNode("/xs:schema/xs:simpleType[@name='ConditionNameType']/xs:restriction");
        if (restriction != null)
        {
            for (String name : webConditions.keySet())
            {
                restriction.addElement("xs:enumeration").addAttribute("value", name);
            }
        }
    }

    private static class PluginLSResourceResolver implements LSResourceResolver
    {
        private final Plugin plugin;

        private PluginLSResourceResolver(Plugin plugin)
        {
            this.plugin = plugin;
        }

        @Override
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI)
        {
            final String resource = systemId;
            final InputSupplier<InputStream> inputSupplier = new InputSupplier<InputStream>()
            {
                @Override
                public InputStream getInput() throws IOException
                {
                    return plugin.getResourceAsStream("/xsd/" + resource);
                }
            };
            return new InputStreamSupplierLSInput(systemId, publicId, inputSupplier);
        }
    }

    private static final class LazyToString<T>
    {
        private final Supplier<T> supplier;

        private LazyToString(Supplier<T> supplier)
        {
            this.supplier = checkNotNull(supplier);
        }

        static <T> LazyToString<T> of(Supplier<T> s)
        {
            return new LazyToString<T>(s);
        }

        @Override
        public String toString()
        {
            return supplier.get().toString();
        }
    }
}