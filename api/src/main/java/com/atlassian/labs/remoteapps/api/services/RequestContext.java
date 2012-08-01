package com.atlassian.labs.remoteapps.api.services;

public interface RequestContext
{
    String getClientKey();

    String getUserId();

    void setClientKey(String clientKey);

    void setUserId(String userId);

    public void clear();
}
