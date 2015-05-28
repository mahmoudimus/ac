package com.atlassian.plugin.connect.core.capabilities.client;

import java.io.IOException;
import java.util.List;

import com.atlassian.plugin.connect.modules.beans.ModuleBean;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;

/**
 * @since 1.0
 */
public class ConnectModuleSetResponseHandler<T extends ModuleBean> implements ResponseHandler<List<T>>
{
    protected final ResponseHandler<String> basicHandler = new BasicResponseHandler();
    
    @Override
    public List<T> handleResponse(HttpResponse response) throws ClientProtocolException, IOException
    {
        return null;
    }

}
