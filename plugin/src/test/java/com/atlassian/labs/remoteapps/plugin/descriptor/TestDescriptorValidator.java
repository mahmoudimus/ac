package com.atlassian.labs.remoteapps.plugin.descriptor;

import com.atlassian.labs.remoteapps.plugin.PermissionManager;
import com.atlassian.labs.remoteapps.spi.permission.Permission;
import com.atlassian.labs.remoteapps.spi.schema.Schema;
import com.atlassian.labs.remoteapps.plugin.product.ProductAccessor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;

import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.parseDocument;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestDescriptorValidator
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
    RemoteAppDescriptorValidatorProvider remoteAppDescriptorValidatorProvider;

    @Mock
    PluginDescriptorValidatorProvider pluginDescriptorValidatorProvider;

    @Mock
    PermissionManager permissionManager;


    private DescriptorValidator descriptorValidator;

    @Before
    public void setUp()
    {
        initMocks(this);
        when(permissionManager.getPermissions()).thenReturn(Collections.<Permission>emptySet());
        
        when(pluginRetrievalService.getPlugin()).thenReturn(plugin);
        when(remoteAppDescriptorValidatorProvider.getRootElementName()).thenReturn("RemoteAppType");
        when(pluginDescriptorValidatorProvider.getRootElementName()).thenReturn("RemoteAppType");

        descriptorValidator = new DescriptorValidator(pluginRetrievalService, productAccessor,
                webResourceManager, permissionManager, remoteAppDescriptorValidatorProvider, pluginDescriptorValidatorProvider);
        when(remoteAppDescriptorValidatorProvider.getModuleSchemas()).thenReturn(Collections.<Schema>emptyList());
        when(plugin.getResource("/xsd/common.xsd")).thenReturn(getClass().getResource("/xsd/common.xsd"));
    }
    
    @Test
    public void testNoModulesOneInclude()
    {
        when(remoteAppDescriptorValidatorProvider.getSchemaUrl()).thenReturn(getClass().getResource("root-one-include.xsd"));
        when(plugin.getResource("/xsd/first-child.xsd")).thenReturn(getClass().getResource("first-child.xsd"));
        String doc = descriptorValidator.getRemoteAppSchema();
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

        when(remoteAppDescriptorValidatorProvider.getSchemaUrl()).thenReturn(getClass().getResource("root-one-include.xsd"));
        when(plugin.getResource("/xsd/module-child.xsd")).thenReturn(getClass().getResource("module-child.xsd"));
        when(plugin.getResource("/xsd/first-child.xsd")).thenReturn(getClass().getResource("first-child.xsd"));

        when(remoteAppDescriptorValidatorProvider.getModuleSchemas()).thenReturn(asList(schema));
        String doc = descriptorValidator.getRemoteAppSchema();
        assertSnippets(doc, "ModuleChildType", "name=\"ModuleType", "name=\"FirstChildType", "RootType", "name=\"module1\"");
        

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
}
