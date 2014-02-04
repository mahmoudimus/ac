package com.atlassian.plugin.connect.plugin.installer;

public interface ConnectAddonRegistry
{

    void removeAll(String pluginKey);

    void storeDescriptor(String pluginKey, String json);

    void removeDescriptor(String pluginKey);

    String getDescriptor(String pluginKey);

    boolean hasDescriptor(String pluginKey);

    void storeBaseUrl(String pluginKey, String url);

    void removeBaseUrl(String pluginKey);

    String getBaseUrl(String pluginKey);

    boolean hasBaseUrl(String pluginKey);

    void storeSecret(String pluginKey, String secret);

    void removeSecret(String pluginKey);

    String getSecret(String pluginKey);

    boolean hasSecret(String pluginKey);
}
