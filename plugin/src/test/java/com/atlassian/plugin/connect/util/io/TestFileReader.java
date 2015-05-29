package com.atlassian.plugin.connect.util.io;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class TestFileReader
{
    public static String readAddonTestFile(String fileName) throws IOException
    {
        // old version of io utils in the products require us to use this form of toString
        return IOUtils.toString(
                new InputStreamReader(TestFileReader.class.getClassLoader().getResourceAsStream("testfiles/capabilities/" + fileName),
                        Charset.forName("UTF-8")));
    }
}
