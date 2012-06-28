package com.atlassian.labs.remoteapps.api;

import org.dom4j.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Set;

import static com.atlassian.labs.remoteapps.api.Utils.printNode;
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
        formatConverter = new FormatConverter(new FormatConverter.ModuleKeyProvider()
        {
            @Override
            public Set<String> getModuleKeys()
            {
                return newHashSet("descriptor", "oauth", "permissions", "vendor", "general-page", "macro");
            }
        });
    }
    @Test
    public void testYamlToXml() throws IOException
    {
        Document docFromYaml = formatConverter.toDocument("urn:foo", "text/yaml", Utils.loadResourceAsString(getClass(),
                "valid.yaml"));
        Document docFromXml = formatConverter.toDocument("urn:foo", "text/xml",
                Utils.loadResourceAsString(getClass(), "valid.xml"));
        Assert.assertEquals(printNode(docFromXml), printNode(docFromYaml));
    }

    @Test
    public void testJsonToXml() throws IOException
    {
        Document docFromYaml = formatConverter.toDocument("urn:foo", "application/json", Utils.loadResourceAsString(
                getClass(), "valid.json"));
        Document docFromXml = formatConverter.toDocument("urn:foo", "text/xml",
                Utils.loadResourceAsString(getClass(), "valid.xml"));
        Assert.assertEquals(printNode(docFromXml), printNode(docFromYaml));
    }
}
