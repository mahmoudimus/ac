package com.atlassian.plugin.connect.plugin.capabilities.client;

import java.io.IOException;
import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.beans.CapabilityBean;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;

/**
 * @since 1.0
 */
public class ConnectCapabilitySetResponseHandler<T extends CapabilityBean> implements ResponseHandler<List<T>>
{
    protected final ResponseHandler<String> basicHandler = new BasicResponseHandler();
    
    @Override
    public List<T> handleResponse(HttpResponse response) throws ClientProtocolException, IOException
    {
        return null;
    }

}
