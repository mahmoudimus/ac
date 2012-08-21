package com.atlassian.labs.remoteapps.host.common.service;

import com.atlassian.labs.remoteapps.api.service.RequestContext;
import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;

public class DefaultRequestContext implements RequestContext
{
    private static final ThreadLocal<String> clientKeyHolder = new ThreadLocal<String>();
    private static final ThreadLocal<String> userIdHolder = new ThreadLocal<String>();

    private SignedRequestHandler signedRequestHandler;

    public DefaultRequestContext(SignedRequestHandler signedRequestHandler)
    {
        this.signedRequestHandler = signedRequestHandler;
    }

    @Override
    public String getClientKey()
    {
        return clientKeyHolder.get();
    }

    public void setClientKey(String clientKey)
    {
        clientKeyHolder.set(clientKey);
    }

    @Override
    public String getUserId()
    {
        return userIdHolder.get();
    }

    public void setUserId(String userId)
    {
        userIdHolder.set(userId);
    }

    @Override
    public String getHostBaseUrl()
    {
        return signedRequestHandler.getHostBaseUrl(getClientKey());
    }

    @Override
    public void clear()
    {
        clientKeyHolder.remove();
        userIdHolder.remove();
    }
}
