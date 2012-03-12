package com.atlassian.labs.remoteapps;

import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.modules.external.Schema;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.parseDocument;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestDescriptorValidator
{
    @Mock
    ModuleGeneratorManager moduleGeneratorManager;
    
    @Mock
    ProductAccessor productAccessor;
    
    @Mock
    PluginRetrievalService pluginRetrievalService;
    
    @Mock
    ApplicationProperties applicationProperties;

    @Mock
    Plugin plugin;

    @Mock
    WebResourceManager webResourceManager;

    @Before
    public void setUp()
    {
        initMocks(this);
        
        when(pluginRetrievalService.getPlugin()).thenReturn(plugin);
    }
    
    @Test
    public void testNoModulesOneInclude()
    {
        when(plugin.getResource("/xsd/remote-app.xsd")).thenReturn(getClass().getResource("root-one-include.xsd"));
        when(plugin.getResource("/xsd/first-child.xsd")).thenReturn(getClass().getResource("first-child.xsd"));
        when(moduleGeneratorManager.getAllValidatableGenerators()).thenReturn(
                Lists.<RemoteModuleGenerator>newArrayList());
        DescriptorValidator validator = new DescriptorValidator(moduleGeneratorManager, applicationProperties,
                pluginRetrievalService, productAccessor, webResourceManager);
        String doc = validator.getSchema();
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
        when(schema.getId()).thenReturn("module1");
        when(schema.getMaxOccurs()).thenReturn("0");

        RemoteModuleGenerator moduleGenerator = mock(RemoteModuleGenerator.class);
        when(moduleGenerator.getSchema()).thenReturn(schema);
        when(moduleGenerator.getType()).thenReturn("module1");
        when(plugin.getResource("/xsd/remote-app.xsd")).thenReturn(getClass().getResource("root-one-include.xsd"));
        when(plugin.getResource("/xsd/module-child.xsd")).thenReturn(getClass().getResource("module-child.xsd"));
        when(plugin.getResource("/xsd/first-child.xsd")).thenReturn(getClass().getResource("first-child.xsd"));
        
        when(moduleGeneratorManager.getAllValidatableGenerators()).thenReturn(
                Lists.<RemoteModuleGenerator>newArrayList(moduleGenerator));
        DescriptorValidator validator = new DescriptorValidator(moduleGeneratorManager, applicationProperties,
                pluginRetrievalService, productAccessor, webResourceManager);
        String doc = validator.getSchema();
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
