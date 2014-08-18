package com.atlassian.plugin.connect.spi.event;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.atlassian.plugin.connect.spi.APITestUtil;

import static org.mockito.Mockito.when;

public class ScopedRequestEventTest
{
    @Mock 
    private HttpServletRequest rq;
    
    @Before
    public void setup()
    {
        this.rq = Mockito.mock(HttpServletRequest.class);
    }
    

    @Test
    public void testJIRARestUrlsShouldBeTrimmed()
    {
        String url = "http://example.atlassian.net/rest/api/2/user/avatar/1938378";
        String expected = "api/2/user/";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq);
        assertEquals(expected, event.getHttpRequestUri());
    }

    @Test
    public void testConfluenceRestUrlsShouldBeTrimmed()
    {
        String url = "http://example.atlassian.net/confluence/rest/api/content/1384754";
        String expected = "api/content/";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq);
        assertEquals(expected, event.getHttpRequestUri());
    }

    @Test
    public void testACRestUrlsShouldBeTrimmed()
    {
        String url = "http://example.atlassian.net/confluence/atlassian-connect/rest/api/foobar/secret";
        String expected = "api/foobar/";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq);
        assertEquals(expected, event.getHttpRequestUri());
    }

    @Test
    public void testACRestUrlsWithQueryParametersShouldBeTrimmed()
    {
        String url = "http://example.atlassian.net/confluence/atlassian-connect/rest/api/foobar?baz=blah&secret=pyramid";
        String expected = "api/foobar/";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq);
        assertEquals(expected, event.getHttpRequestUri());
    }

    @Test
    public void testJIRASoapUrlsShouldIncludeMethod() throws IOException
    {
        String url = "http://example.atlassian.net/rpc/soap/jirasoapservice-v2";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        when(rq.getInputStream()).thenReturn(new MockServletInputStream(APITestUtil.createSoapRpcPayload("method")));
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq);
        assertEquals("/rpc/soap/jirasoapservice-v2/method", event.getHttpRequestUri());
    }

    @Test
    public void testJIRAJsonRpcUrlsShouldIncludeMethod() throws IOException
    {
        String url = "http://example.atlassian.net/rpc/json-rpc/jirasoapservice-v2";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        when(rq.getInputStream()).thenReturn(new MockServletInputStream(APITestUtil.createJsonRpcPayload("method")));
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq);
        assertEquals("/rpc/json-rpc/jirasoapservice-v2/method", event.getHttpRequestUri());
    }


    @Test
    public void testConfluenceJsonRpcUrlsShouldIncludeMethod() throws IOException
    {
        String url = "http://example.atlassian.net/confluence/rpc/json-rpc/confluenceservice-v2";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        when(rq.getInputStream()).thenReturn(new MockServletInputStream(APITestUtil.createJsonRpcPayload("method")));
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq);
        assertEquals("/confluence/rpc/json-rpc/confluenceservice-v2/method", event.getHttpRequestUri());
    }
    
    @Test
    public void testConfluenceXmlRpcUrlsShouldIncludeMethod() throws IOException
    {
        String url = "http://example.atlassian.net/confluence/rpc/xmlrpc";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        when(rq.getInputStream()).thenReturn(new MockServletInputStream(APITestUtil.createXmlRpcPayload("method")));
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq);
        assertEquals("/confluence/rpc/xmlrpc/method", event.getHttpRequestUri());
    }
    
    
    private class MockServletInputStream extends ServletInputStream
    {
        private InputStream in;
        
        public MockServletInputStream(String body) {
            this.in = IOUtils.toInputStream(body);
        }

        @Override
        public int read() throws IOException
        {
            return in.read();
        }
        
    }
}
