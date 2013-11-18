package com.atlassian.json.schema.scanner;

import java.io.File;

import com.atlassian.json.schema.scanner.model.InterfaceList;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;

public class InterfaceImplementationScanner
{
    public void scan(String basePackage, String classpath, File outputFile) throws Exception
    {
        InterfaceParser parser = new InterfaceParser();
        InterfaceList list = parser.parse(basePackage, classpath);

        Gson gson = new Gson();

        Files.write(gson.toJson(list),outputFile, Charsets.UTF_8);
    }
}
