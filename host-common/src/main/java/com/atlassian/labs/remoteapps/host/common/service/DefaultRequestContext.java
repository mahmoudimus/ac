package com.atlassian.labs.remoteapps.host.common.service;

import com.atlassian.labs.remoteapps.api.service.RequestContext;
import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;

import javax.servlet.http.HttpServletRequest;

public class DefaultRequestContext implements RequestContext
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

    private RequestData getRequestData()
    {
        RequestData data = requestContextHolder.get();
        return data != null ? data : EMPTY_DATA;
    }

    private void setRequestData(RequestData data)
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

    public <P, R> RequestCallable<P, R> createCallableForCurrentRequest(final RequestCallable<P, R> callable)
    {
        final RequestData old = getRequestData();
        return new RequestCallable<P, R>()
        {
            @Override
            public R call(P contextParameter)
            {
                RequestData current = getRequestData();
                try
                {
                    setRequestData(old);
                    return callable.call(contextParameter);
                }
                finally
                {
                    setRequestData(current);
                }
            }
        };
    }

    @Override
    public String getHostBaseUrl()
    {
        return signedRequestHandler.getHostBaseUrl(getClientKey());
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

    public void clear()
    {
        requestContextHolder.remove();
    }

    public static interface RequestCallable<P,R>
    {
        R call(P contextParameter);
    }

    private static class RequestData
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
