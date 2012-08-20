package com.atlassian.labs.remoteapps.api.service;

public interface RequestContext
{
    String getClientKey();

    String getUserId();

    String getHostBaseUrl();

    public void clear();
}
