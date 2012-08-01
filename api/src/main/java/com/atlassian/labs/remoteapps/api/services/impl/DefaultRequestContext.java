package com.atlassian.labs.remoteapps.api.services.impl;

import com.atlassian.labs.remoteapps.api.services.RequestContext;

public class DefaultRequestContext implements RequestContext
{
    private static final ThreadLocal<String> clientKeyHolder = new ThreadLocal<String>();
    private static final ThreadLocal<String> userIdHolder = new ThreadLocal<String>();

    @Override
    public String getClientKey()
    {
        return clientKeyHolder.get();
    }

    @Override
    public void setClientKey(String clientKey)
    {
        clientKeyHolder.set(clientKey);
    }

    @Override
    public String getUserId()
    {
        return userIdHolder.get();
    }

    @Override
    public void setUserId(String userId)
    {
        userIdHolder.set(userId);
    }

    @Override
    public void clear()
    {
        clientKeyHolder.remove();
        userIdHolder.remove();
    }
}
