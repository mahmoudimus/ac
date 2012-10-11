package com.atlassian.plugin.remotable.api.service;

public interface RequestContext
{
    String getClientKey();

    String getUserId();

    String getHostBaseUrl();
}
