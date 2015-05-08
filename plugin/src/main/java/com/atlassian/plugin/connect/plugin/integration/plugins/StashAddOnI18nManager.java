package com.atlassian.plugin.connect.plugin.integration.plugins;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;

@StashComponent
public class StashAddOnI18nManager implements ConnectAddonI18nManager
{
    @Override
    public void add(String addonKey, Properties i18nProperties) throws IOException
    {

    }

    @Override
    public void add(String addonKey, Map<String, String> i18nMap) throws IOException
    {

    }
}
