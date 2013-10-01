package com.atlassian.plugin.connect.plugin.capabilities.http;

import java.io.IOException;
import java.util.List;

import com.atlassian.plugin.connect.api.capabilities.beans.CapabilityBean;
import com.atlassian.plugin.connect.api.capabilities.beans.CapabilitySetContainer;

import com.google.common.annotations.VisibleForTesting;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;

/**
 * @since version
 */
public class ConnectCapabilitySetResponseHandler<T extends CapabilityBean> implements ResponseHandler<List<T>>
{
    protected final ResponseHandler<String> basicHandler = new BasicResponseHandler();
    
    @Override
    public List<T> handleResponse(HttpResponse response) throws ClientProtocolException, IOException
    {
        return null;
    }

    @VisibleForTesting
    CapabilitySetContainer<T> parseBody(String responseBody)
    {
        return null;
    }
}
