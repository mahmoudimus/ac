package com.atlassian.plugin.connect.test.plugin.capabilities;

import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.nio.charset.Charset;

public class TestFileReader
{
    public static String readAddonTestFile(String fileName) throws IOException
    {
        return IOUtils.toString(TestFileReader.class.getClassLoader().getResourceAsStream("testfiles/capabilities/" + fileName), Charset.forName("UTF-8"));
    }
}
