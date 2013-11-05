package com.atlassian.plugin.connect.plugin.capabilities;

import java.io.IOException;

import com.opensymphony.util.FileUtils;

import org.springframework.core.io.DefaultResourceLoader;

public class TestFileReader
{
    public static String readCapabilitiesTestFile(String fileName) throws IOException
    {
        return FileUtils.readFile(new DefaultResourceLoader().getResource("classpath:/testfiles/capabilities/" + fileName).getFile());
    }
}
