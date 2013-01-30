package com.atlassian.plugin.remotable.plugin.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.remotable.plugin.PermissionManager;
import com.atlassian.plugin.remotable.spi.permission.Permission;
import com.atlassian.plugin.remotable.spi.product.ProductAccessor;
import com.atlassian.plugin.schema.spi.Schema;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.InputSupplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.parseDocument;
import static com.google.common.io.CharStreams.newReaderSupplier;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TestDescriptorValidator
{
    @Mock
    ProductAccessor productAccessor;

    @Mock
    PluginRetrievalService pluginRetrievalService;

    @Mock
    Plugin plugin;

    @Mock
    WebResourceManager webResourceManager;

    @Mock
    PluginDescriptorValidatorProvider pluginDescriptorValidatorProvider;

    @Mock
    PermissionManager permissionManager;

    private DescriptorValidator descriptorValidator;

    @Before
    public void setUp()
    {
        when(permissionManager.getPermissions()).thenReturn(Collections.<Permission>emptySet());

        when(pluginRetrievalService.getPlugin()).thenReturn(plugin);
        when(pluginDescriptorValidatorProvider.getRootElementName()).thenReturn("AtlassianPluginType");

        descriptorValidator = new DescriptorValidator(pluginRetrievalService, productAccessor,
                webResourceManager, permissionManager, pluginDescriptorValidatorProvider);
        when(pluginDescriptorValidatorProvider.getModuleSchemas()).thenReturn(Collections.<Schema>emptyList());
        when(plugin.getResource("/xsd/common.xsd")).thenReturn(getClass().getResource("/xsd/common.xsd"));
    }

    @Test
    public void testNoModulesOneInclude()
    {
        when(pluginDescriptorValidatorProvider.getSchemaUrl()).thenReturn(getClass().getResource("root-one-include.xsd"));
        when(plugin.getResource("/xsd/first-child.xsd")).thenReturn(getClass().getResource("first-child.xsd"));
        String doc = descriptorValidator.getPluginSchema();
        assertTrue(doc.contains("RootType"));
        assertTrue(doc.contains("ChildType"));
        assertFalse(doc.contains(":include"));
    }

    @Test
    public void testOneModuleOneEmbeddedInclude()
    {
        Schema schema = mock(Schema.class);
        when(schema.getDocument()).thenReturn(parseDocument(
                getClass().getResource("module-one-include.xsd")));
        when(schema.getComplexType()).thenReturn("ModuleType");
        when(schema.getFileName()).thenReturn("module-one-include.xsd");
        when(schema.getElementName()).thenReturn("module1");
        when(schema.getMaxOccurs()).thenReturn("0");

        when(pluginDescriptorValidatorProvider.getSchemaUrl()).thenReturn(getClass().getResource("root-one-include.xsd"));
        when(plugin.getResource("/xsd/module-child.xsd")).thenReturn(getClass().getResource("module-child.xsd"));
        when(plugin.getResource("/xsd/first-child.xsd")).thenReturn(getClass().getResource("first-child.xsd"));

        when(pluginDescriptorValidatorProvider.getModuleSchemas()).thenReturn(asList(schema));
        String doc = descriptorValidator.getPluginSchema();
        assertSnippets(doc, "ModuleChildType", "name=\"ModuleType", "name=\"FirstChildType", "RootType", "name=\"module1\"");
    }

    @Test
    public void testValidateMinimalPluginDescriptor() throws Exception
    {
        final javax.xml.validation.Schema schema = getSchema("atlassian-plugin.xsd");

        validate(schema,
                "<atlassian-plugin key='test' name='Test'>",
                "  <plugin-info>",
                "    <version>test-version</version>",
                "  </plugin-info>",
                "</atlassian-plugin>");
    }

    @Test
    public void testValidateRestricts() throws Exception
    {
        final javax.xml.validation.Schema schema = getSchema("test-restricts.xsd");

        validate(schema,
                "<restrict application='jira'></restrict>");
    }

    @Test
    public void testValidateRestrictsVersionRanges() throws Exception
    {
        final javax.xml.validation.Schema schema = getSchema("test-restricts.xsd");

        validate(schema, "<restrict application='jira'></restrict>");
        validate(schema, "<restrict application='jira' version='1.0'></restrict>");
        validate(schema, "<restrict application='jira' version='[1.0,2.0)'></restrict>");
        validate(schema, "<restrict application='jira' version='(1.0,2.0]'></restrict>");
        validate(schema, "<restrict application='jira' version='[1.0,)'></restrict>");
        validate(schema, "<restrict application='jira' version='(,2.0]'></restrict>");
    }

    private void validate(javax.xml.validation.Schema schema, String... xml) throws SAXException, IOException
    {
        schema.newValidator().validate(new StreamSource(newReaderSupplier(Joiner.on('\n').join(xml)).getInput()));
    }

    private javax.xml.validation.Schema getSchema(String xsd) throws IOException
    {
        return DescriptorValidator.getSchema(newReaderSupplier(new XsdResourceInputStreamSupplier(xsd), Charsets.UTF_8), new XsdResourceLSResourceResolver());
    }

    private void assertSnippets(String doc, String... snippets)
    {
        for (String snippet : snippets)
        {
            int curPos = doc.indexOf(snippet);
            assertTrue("Text '" + snippet + "' not found: " + doc, curPos > -1);
            assertTrue("Text '" + snippet + "' found more than once: " + doc, doc.indexOf(snippet, curPos + 1) == -1);
        }
        assertFalse(doc.contains(":include"));
    }

    private static class XsdResourceInputStreamSupplier implements InputSupplier<InputStream>
    {
        private final String xsd;

        public XsdResourceInputStreamSupplier(String xsd)
        {
            this.xsd = xsd;
        }

        @Override
        public InputStream getInput() throws IOException
        {
            return this.getClass().getResourceAsStream("/xsd/" + xsd);
        }
    }

    private static class XsdResourceLSResourceResolver implements LSResourceResolver
    {
        @Override
        public LSInput resolveResource(String type, String namespaceURI, String publicId, final String systemId, String baseURI)
        {
            return new InputStreamSupplierLSInput(systemId, publicId, new XsdResourceInputStreamSupplier(systemId));
        }
    }
}
