package com.atlassian.plugin.remotable.plugin.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.plugin.PermissionManager;
import com.atlassian.plugin.remotable.spi.InstallationFailedException;
import com.atlassian.plugin.remotable.spi.permission.Permission;
import com.atlassian.plugin.remotable.spi.permission.scope.ApiResourceInfo;
import com.atlassian.plugin.remotable.spi.permission.scope.ApiScope;
import com.atlassian.plugin.remotable.spi.product.ProductAccessor;
import com.atlassian.plugin.remotable.spi.util.Dom4jUtils;
import com.atlassian.plugin.schema.spi.Schema;
import com.atlassian.plugin.schema.spi.SchemaDocumented;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.parseDocument;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.printNode;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Builds a schema and validates descriptors with it.  Supports remote plugin and plugin descriptors.
 */
@Component
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
        final InstallationMode installationMode = InstallationMode.LOCAL; // to validate we use the local
        final boolean useNamespace = document.getRootElement().getNamespaceURI().equals(pluginDescriptorValidatorProvider.getSchemaNamespace(installationMode));

        final String builtSchema = buildSchema(pluginDescriptorValidatorProvider, useNamespace, installationMode);
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

    public String getPluginSchema(InstallationMode installationMode)
    {
        try
        {
            return buildSchema(pluginDescriptorValidatorProvider, true, installationMode);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    private String buildSchema(DescriptorValidatorProvider descriptorValidatorProvider, boolean usesNamespace, InstallationMode installationMode)
    {
        return buildSchema(parseDocument(descriptorValidatorProvider.getSchemaUrl()), descriptorValidatorProvider, usesNamespace, installationMode);
    }

    private String buildSchema(Document schema, DescriptorValidatorProvider descriptorValidatorProvider, boolean usesNamespace, InstallationMode installationMode)
    {
        Element root = schema.getRootElement();

        addNamespace(schema, descriptorValidatorProvider, usesNamespace, installationMode);

        final Set<String> includedDocIds = newHashSet();

        processIncludes(root.getDocument(), includedDocIds);

        addModules(schema, descriptorValidatorProvider, installationMode, includedDocIds);

        addPermissions(schema);

        addXslStyleSheet(schema);

        return printNode(root.getDocument());
    }

    private void addModules(Document schema, DescriptorValidatorProvider descriptorValidatorProvider, InstallationMode installationMode, Set<String> includedDocIds)
    {
        final Element choiceOfModules = selectSingleNode(schema, "/xs:schema/xs:complexType[@name='%s']//xs:choice", descriptorValidatorProvider.getRootElementName());
        for (final Schema moduleSchema : descriptorValidatorProvider.getModuleSchemas(installationMode))
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

    private void addPermissions(Document schema)
    {
        final Element permissionsType = selectSingleNode(schema, "/xs:schema/xs:simpleType[@name='PermissionValueType']/xs:restriction");
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
    }

    public static Element addSchemaDocumentation(Element source, SchemaDocumented generator)
    {
        Element doc = source.addElement("xs:annotation").addElement("xs:documentation");
        Element name = doc.addElement("name");
        if (generator.getName() != null)
        {
            name.setText(generator.getName());
        }
        Element desc = doc.addElement("description");
        if (generator.getDescription() != null)
        {
            desc.setText(generator.getDescription());
        }
        return doc;
    }

    private static void addNamespace(Document schema, DescriptorValidatorProvider descriptorValidatorProvider, boolean usesNamespace, InstallationMode installationMode)
    {
        if (usesNamespace)
        {
            final String ns = descriptorValidatorProvider.getSchemaNamespace(installationMode);
            final Element root = schema.getRootElement();
            root.addAttribute("targetNamespace", ns);
            root.addAttribute("xmlns", ns);
        }
    }

    private void addXslStyleSheet(Document schema)
    {
        final Map<String, String> arguments = Maps.newHashMap();
        arguments.put("type", "text/xsl");
        arguments.put("href", getXslStyleSheetUrl());

        schema.content().add(0, new DocumentFactory().createProcessingInstruction("xml-stylesheet", arguments));
    }

    private String getXslStyleSheetUrl()
    {
        return webResourceManager.getStaticPluginResource("com.atlassian.labs.remoteapps-plugin:schema-xsl", "xs3p.xsl", UrlMode.ABSOLUTE);
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