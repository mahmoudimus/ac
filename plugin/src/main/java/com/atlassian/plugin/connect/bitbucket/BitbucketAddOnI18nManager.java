package com.atlassian.plugin.connect.bitbucket;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.atlassian.plugin.connect.spi.integration.plugins.ConnectAddonI18nManager;
import com.atlassian.plugin.spring.scanner.annotation.component.BitbucketComponent;

@BitbucketComponent
public class BitbucketAddOnI18nManager implements ConnectAddonI18nManager
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
