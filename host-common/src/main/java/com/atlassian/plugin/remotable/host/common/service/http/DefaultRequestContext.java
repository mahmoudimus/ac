package com.atlassian.plugin.remotable.host.common.service.http;

import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;

import javax.servlet.http.HttpServletRequest;

public final class DefaultRequestContext implements RequestContext
{
    private static final ThreadLocal<RequestData> requestContextHolder = new ThreadLocal<RequestData>();
    private static final RequestData EMPTY_DATA = new RequestData(null, null, null);

    private final SignedRequestHandler signedRequestHandler;

    public DefaultRequestContext(SignedRequestHandler signedRequestHandler)
    {
        this.signedRequestHandler = signedRequestHandler;
    }

    @Override
    public String getClientKey()
    {
        return getRequestData().getClientKey();
    }

    public void setClientKey(String clientKey)
    {
        RequestData data = getRequestData();
        setRequestData(new RequestData(data.getRequest(), clientKey, data.getUserId()));
    }

    static RequestData getRequestData()
    {
        RequestData data = requestContextHolder.get();
        return data != null ? data : EMPTY_DATA;
    }

    static void setRequestData(RequestData data)
    {
        if (data == EMPTY_DATA)
        {
            clear();
        }
        else
        {
            requestContextHolder.set(data);
        }
    }

    @Override
    public String getUserId()
    {
        return getRequestData().getUserId();
    }

    public void setUserId(String userId)
    {
        RequestData data = getRequestData();
        setRequestData(new RequestData(data.getRequest(), data.getClientKey(), userId));
    }

    @Override
    public String getHostBaseUrl()
    {
        String clientKey = getClientKey();
        return clientKey != null ? signedRequestHandler.getHostBaseUrl(clientKey) : null;
    }

    public HttpServletRequest getRequest()
    {
        return requestContextHolder.get().getRequest();
    }

    public void setRequest(HttpServletRequest request)
    {
        RequestData data = getRequestData();
        setRequestData(new RequestData(request, data.getClientKey(), data.getUserId()));
    }

    public static void clear()
    {
        requestContextHolder.remove();
    }

    final static class RequestData
    {
        private final String clientKey;
        private final String userId;
        private final HttpServletRequest request;

        private RequestData(HttpServletRequest request, String clientKey, String userId)
        {
            this.request = request;
            this.clientKey = clientKey;
            this.userId = userId;
        }

        public HttpServletRequest getRequest()
        {
            return request;
        }

        public String getClientKey()
        {
            return clientKey;
        }

        public String getUserId()
        {
            return userId;
        }
    }

}
