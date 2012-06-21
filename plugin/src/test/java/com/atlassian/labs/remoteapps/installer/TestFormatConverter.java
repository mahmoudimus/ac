package com.atlassian.labs.remoteapps.installer;

import com.atlassian.labs.remoteapps.ModuleGeneratorManager;
import org.dom4j.Document;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.labs.remoteapps.test.Utils.loadResourceAsString;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.printNode;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestFormatConverter
{
    private FormatConverter formatConverter;
    
    @Before
    public void setUp()
    {
        ModuleGeneratorManager moduleGeneratorManager = mock(ModuleGeneratorManager.class);
        when(moduleGeneratorManager.getModuleGeneratorKeys()).thenReturn(newHashSet(
                "descriptor", "oauth", "permissions", "vendor", "general-page", "macro"));
        formatConverter = new FormatConverter(moduleGeneratorManager);
    }
    @Test
    public void testYamlToXml() throws IOException
    {
        Document docFromYaml = formatConverter.toDocument("urn:foo", "text/yaml", loadResourceAsString(
                getClass(), "valid.yaml"));
        Document docFromXml = formatConverter.toDocument("urn:foo", "text/xml",
                loadResourceAsString(getClass(), "valid.xml"));
        assertEquals(printNode(docFromXml), printNode(docFromYaml));
    }

    @Test
    public void testJsonToXml() throws IOException
    {
        Document docFromYaml = formatConverter.toDocument("urn:foo", "application/json", loadResourceAsString(
                getClass(), "valid.json"));
        Document docFromXml = formatConverter.toDocument("urn:foo", "text/xml",
                loadResourceAsString(getClass(), "valid.xml"));
        assertEquals(printNode(docFromXml), printNode(docFromYaml));
    }
}
