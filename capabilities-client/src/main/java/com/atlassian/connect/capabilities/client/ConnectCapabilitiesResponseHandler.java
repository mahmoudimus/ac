package com.atlassian.connect.capabilities.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;
import org.joda.time.DateTime;

/**
 * @since version
 */
public class ConnectCapabilitiesResponseHandler implements ResponseHandler<RemoteApplicationWithCapabilities>
{
    protected final ResponseHandler<String> basicHandler = new BasicResponseHandler();
    
    @Override
    public RemoteApplicationWithCapabilities handleResponse(HttpResponse response) throws ClientProtocolException, IOException
    {
        return (null == response.getEntity() ? null: parseBody(response.getEntity().getContent()));
    }

    
    public RemoteApplicationWithCapabilities parseBody(InputStream in)
    {
        return CapabilitiesGsonFactory.getGson().fromJson(new BufferedReader(new InputStreamReader(in)),RemoteApplicationWithCapabilitiesImpl.class);
    }
}
