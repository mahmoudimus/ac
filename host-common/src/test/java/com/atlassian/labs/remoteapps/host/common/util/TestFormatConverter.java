package com.atlassian.labs.remoteapps.host.common.util;

import com.atlassian.labs.remoteapps.host.common.Utils;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
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
    @Ignore
    public void testYamlToXml() throws IOException
    {
        Document docFromYaml = formatConverter.toDocument("urn:foo", "text/yaml", Utils.loadResourceAsString(getClass(),
                "valid.yaml"));
        Document docFromXml = formatConverter.toDocument("urn:foo", "text/xml",
                Utils.loadResourceAsString(getClass(), "valid.xml"));

        FileUtils.writeStringToFile(new File("/tmp/original.txt"), printNode(docFromXml));
        FileUtils.writeStringToFile(new File("/tmp/actual.txt"), printNode(docFromYaml));
        Assert.assertEquals(printNode(docFromXml), printNode(docFromYaml));
    }

    @Test
    @Ignore
    public void testJsonToXml() throws IOException
    {
        Document docFromYaml = formatConverter.toDocument("urn:foo", "application/json", Utils.loadResourceAsString(
                getClass(), "valid.json"));
        Document docFromXml = formatConverter.toDocument("urn:foo", "text/xml",
                Utils.loadResourceAsString(getClass(), "valid.xml"));
        Assert.assertEquals(printNode(docFromXml), printNode(docFromYaml));
    }
}
