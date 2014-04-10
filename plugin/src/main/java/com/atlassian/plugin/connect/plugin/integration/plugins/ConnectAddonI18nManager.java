package com.atlassian.plugin.connect.plugin.integration.plugins;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public interface ConnectAddonI18nManager
{

    void add(String addonKey, Properties i18nProperties) throws IOException;

    void add(String addonKey, Map<String, String> i18nMap) throws IOException;
}
