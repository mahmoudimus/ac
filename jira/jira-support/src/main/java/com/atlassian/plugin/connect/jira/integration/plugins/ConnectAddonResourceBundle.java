package com.atlassian.plugin.connect.jira.integration.plugins;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectAddonResourceBundle extends ResourceBundle
{
    private ConcurrentHashMap<String,String> i18nProps;
    private static ConnectAddonResourceBundle DEFAULT_INSTANCE;

    public ConnectAddonResourceBundle()
    {
        if(null == DEFAULT_INSTANCE)
        {
            DEFAULT_INSTANCE = this;
            DEFAULT_INSTANCE.i18nProps = new ConcurrentHashMap<String, String>();
        }
    }

    @Override
    protected Object handleGetObject(String key)
    {
        return DEFAULT_INSTANCE.i18nProps.get(key);
    }

    @Override
    public Enumeration<String> getKeys()
    {
        return Collections.enumeration(handleKeySet());
    }

    @Override
    protected Set<String> handleKeySet()
    {
        return DEFAULT_INSTANCE.i18nProps.keySet();
    }

    public void add(Map<String, String> i18nMap)
    {
        DEFAULT_INSTANCE.i18nProps.putAll(i18nMap);
    }
}
