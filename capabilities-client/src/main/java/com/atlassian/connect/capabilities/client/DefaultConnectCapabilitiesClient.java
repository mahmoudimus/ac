package com.atlassian.connect.capabilities.client;


import java.io.IOException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Strings;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.collect.Iterables.contains;

@Component
public class DefaultConnectCapabilitiesClient implements ConnectCapabilitiesClient
{
    private static final Logger logger = LoggerFactory.getLogger(DefaultConnectCapabilitiesClient.class);
    private static final String AC_USER_AGENT = "Atlassian-Connect (1.0)";
    private final HttpClient httpclient;

    @Autowired
    public DefaultConnectCapabilitiesClient(HttpClientFactory httpClientFactory)
    {
        this(httpClientFactory.createHttpClient());
    }

    DefaultConnectCapabilitiesClient(HttpClient httpclient)
    {
        this.httpclient = httpclient;
    }

    @Override
    public RemoteApplicationWithCapabilities getCapabilities(String url)
    {
        try
        {
            return httpclient.execute(createGetRequest(url),new EntityEatingResponseHandler<RemoteApplicationWithCapabilities>(new ConnectCapabilitiesResponseHandler()));
        }
        catch (Exception e)
        {
            logger.error("Failed to request capabilities from '{}': {}", url, e.getMessage());
            logger.debug("Stacktrace: ", e);
            return null;
        }
    }

    @Override
    public <T> T getCapabilitySet(RemoteApplicationWithCapabilities app, String key, ResponseHandler<T> responseHandler)
    {
        String url = null;
        try
        {
            url = app.getCapabilityUrl(key);
            if(Strings.isNullOrEmpty(url))
            {
                logger.error("No capability url found for key: {}", key);
                return null;
            }
            
            return httpclient.execute(createGetRequest(url),new EntityEatingResponseHandler<T>(responseHandler));
        }
        catch (Exception e)
        {
            logger.error("Failed to request capability set from '{}': {}", url, e.getMessage());
            logger.debug("Stacktrace: ", e);
            return null;
        }
    }

    private HttpGet createGetRequest(final String url)
    {
        final HttpGet request = new HttpGet(url);
        request.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        request.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        request.setHeader(HttpHeaders.USER_AGENT, AC_USER_AGENT);
        return request;
    }
}
