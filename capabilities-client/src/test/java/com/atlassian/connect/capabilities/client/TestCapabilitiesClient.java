package com.atlassian.connect.capabilities.client;

import java.util.Collections;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since version
 */
public class TestCapabilitiesClient
{
    @Test
    public void simpleMarshallingWorks() throws Exception
    {
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        HttpClient httpClient = mock(HttpClient.class);
        
        RemoteApplicationWithCapabilitiesImpl expectedApp = new RemoteApplicationWithCapabilitiesImpl("my-app",new DateTime(),"http://www.example.com/capabilities", Collections.EMPTY_MAP);
        
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DateTime.class,new DateTimeTypeAdapter());

        Gson gson = builder.create();
        
        final String json = gson.toJson(expectedApp);
        
        when(httpClient.execute(any(HttpGet.class),any(ResponseHandler.class))).then(new Answer<RemoteApplicationWithCapabilities>()
        {
            @Override
            public RemoteApplicationWithCapabilities answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                try
                {
                    ConnectCapabilitiesResponseHandler handler = new ConnectCapabilitiesResponseHandler();
                    return handler.parseBody(json);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return null;
                }
            }
        });
        
        when(httpClientFactory.createHttpClient()).thenReturn(httpClient);
        
        ConnectCapabilitiesClient client = new DefaultConnectCapabilitiesClient(httpClientFactory);
        RemoteApplicationWithCapabilities app = client.getCapabilities("http://www.example.com/capabilities");
        
        assertEquals(expectedApp,app);
    }

    @Test
    public void noBuildDateWorks() throws Exception
    {
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        HttpClient httpClient = mock(HttpClient.class);

        RemoteApplicationWithCapabilitiesImpl expectedApp = new RemoteApplicationWithCapabilitiesImpl("my-app",null,"http://www.example.com/capabilities", Collections.EMPTY_MAP);

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DateTime.class,new DateTimeTypeAdapter());

        Gson gson = builder.create();

        final String json = gson.toJson(expectedApp);

        when(httpClient.execute(any(HttpGet.class),any(ResponseHandler.class))).then(new Answer<RemoteApplicationWithCapabilities>()
        {
            @Override
            public RemoteApplicationWithCapabilities answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                try
                {
                    ConnectCapabilitiesResponseHandler handler = new ConnectCapabilitiesResponseHandler();
                    return handler.parseBody(json);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return null;
                }
            }
        });

        when(httpClientFactory.createHttpClient()).thenReturn(httpClient);

        ConnectCapabilitiesClient client = new DefaultConnectCapabilitiesClient(httpClientFactory);
        RemoteApplicationWithCapabilities app = client.getCapabilities("http://www.example.com/capabilities");

        assertEquals(expectedApp,app);
    }
}
