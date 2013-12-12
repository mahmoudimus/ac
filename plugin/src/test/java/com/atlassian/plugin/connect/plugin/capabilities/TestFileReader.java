package com.atlassian.plugin.connect.plugin.capabilities;

import com.opensymphony.util.FileUtils;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;

public class TestFileReader
{
    public static String readAddonTestFile(String fileName) throws IOException
    {
        return FileUtils.readFile(new DefaultResourceLoader().getResource("classpath:/testfiles/capabilities/" + fileName).getFile());
    }
}
