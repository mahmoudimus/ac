package com.atlassian.connect.capabilities.client;

import java.io.IOException;

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
        final String responseBody = basicHandler.handleResponse(response);
        return Strings.isNullOrEmpty(responseBody)? null: parseBody(responseBody);
    }

    @VisibleForTesting
    RemoteApplicationWithCapabilities parseBody(String responseBody)
    {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DateTime.class,new DateTimeTypeAdapter());
        
        Gson gson = builder.create();
        return gson.fromJson(responseBody,RemoteApplicationWithCapabilitiesImpl.class);
    }
}
