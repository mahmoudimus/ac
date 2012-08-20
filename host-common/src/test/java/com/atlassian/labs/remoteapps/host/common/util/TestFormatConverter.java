package com.atlassian.labs.remoteapps.host.common.util;

import com.atlassian.labs.remoteapps.host.common.Utils;
import org.dom4j.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.labs.remoteapps.host.common.Utils.printNode;

public class TestFormatConverter
{
    private FormatConverter formatConverter;
    
    @Before
    public void setUp()
    {
        formatConverter = new FormatConverter();
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
