package com.atlassian.plugin.remotable.descriptor;

public interface LocalMountBaseUrlResolver
{
    public static final String BASE_URL_PROP = "localBaseUrl";
    String getLocalMountBaseUrl(String appKey);
}
