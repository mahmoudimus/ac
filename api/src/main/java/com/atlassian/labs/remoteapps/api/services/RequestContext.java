package com.atlassian.labs.remoteapps.api.services;

public interface RequestContext
{
    String getClientKey();

    String getUserId();

    String getHostBaseUrl();

    public void clear();
}
