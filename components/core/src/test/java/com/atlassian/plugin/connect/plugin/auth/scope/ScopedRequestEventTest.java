package com.atlassian.plugin.connect.plugin.auth.scope;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
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
        String url = "/rest/api/2/user/avatar/1938378";
        String expected = "api/2/user";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        when(rq.getContextPath()).thenReturn("");
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq, "");
        assertEquals(expected, event.getHttpRequestUri());
    }

    @Test
    public void testConfluenceRestUrlsShouldBeTrimmed()
    {
        String url = "/confluence/rest/api/content/1384754";
        String expected = "api/content";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        when(rq.getContextPath()).thenReturn("/confluence");
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq, "");
        assertEquals(expected, event.getHttpRequestUri());
    }

    @Test
    public void testACRestUrlsShouldBeTrimmed()
    {
        String url = "/confluence/atlassian-connect/rest/api/foobar/secret";
        String expected = "api/foobar";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        when(rq.getContextPath()).thenReturn("/confluence");
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq, "");
        assertEquals(expected, event.getHttpRequestUri());
    }

    @Test
    public void testACRestUrlsWithQueryParametersShouldBeTrimmed()
    {
        String url = "/confluence/atlassian-connect/rest/api/foobar?baz=blah&secret=pyramid";
        String expected = "api/foobar";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        when(rq.getContextPath()).thenReturn("/confluence");
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq, "");
        assertEquals(expected, event.getHttpRequestUri());
    }

    @Test
    public void JIRARestURLWithNumericVersionShouldBeTrimmed() throws IOException
    {
        String url = "/rest/api/2/attachment/1384754";
        String expected = "api/2/attachment";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        when(rq.getContextPath()).thenReturn("");
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq, "");
        assertEquals(expected, event.getHttpRequestUri());
    }
    @Test
    public void JIRARestURLWithDecimalNumericVersionShouldBeTrimmed() throws IOException
    {
        String url = "/rest/api/2.0/attachment/1384754";
        String expected = "api/2.0/attachment";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        when(rq.getContextPath()).thenReturn("");
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq, "");
        assertEquals(expected, event.getHttpRequestUri());
    }
    @Test
    public void JIRARestURLWithLatestShouldBeTrimmed() throws IOException
    {
        String url = "/rest/api/latest/attachment/1384754";
        String expected = "api/latest/attachment";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        when(rq.getContextPath()).thenReturn("");
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq, "");
        assertEquals(expected, event.getHttpRequestUri());
    }


    @Test
    public void testConfluenceJsonRpcUrlsShouldIncludeMethod() throws IOException
    {
        String url = "/confluence/rpc/json-rpc/confluenceservice-v2";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        when(rq.getInputStream()).thenReturn(new MockServletInputStream(createJsonRpcPayload()));
        when(rq.getContextPath()).thenReturn("/confluence");
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq, "");
        assertEquals("/rpc/json-rpc/confluenceservice-v2/method", event.getHttpRequestUri());
    }

    @Test
    public void testConfluenceJsonRpcLightUrlsShouldIncludeMethod() throws IOException
    {
        String url = "/confluence/rpc/json-rpc/confluenceservice-v2/methodName";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        when(rq.getInputStream()).thenReturn(new MockServletInputStream("some json"));
        when(rq.getContextPath()).thenReturn("/confluence");
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq, "");
        assertEquals("/rpc/json-rpc/confluenceservice-v2/methodName", event.getHttpRequestUri());
    }

    @Test
    public void testConfluenceXmlRpcUrlsShouldIncludeMethod() throws IOException
    {
        String url = "/confluence/rpc/xmlrpc";
        when(rq.getMethod()).thenReturn("GET");
        when(rq.getRequestURI()).thenReturn(url);
        when(rq.getInputStream()).thenReturn(new MockServletInputStream(createXmlRpcPayload()));
        when(rq.getContextPath()).thenReturn("/confluence");
        ScopedRequestEvent event = new ScopedRequestDeniedEvent(rq, "");
        assertEquals("/rpc/xmlrpc/method", event.getHttpRequestUri());
    }

    private String createJsonRpcPayload()
    {
        return "{\"jsonrpc\" : \"2.0\",  \"method\" : \"" + "method" + "\"}";
    }

    private String createXmlRpcPayload()
    {
        return "<?xml version=\"1.0\"?>\n" +
                "<methodCall>\n" +
                "   <methodName>"+ "method" +"</methodName>\n" +
                "</methodCall>";
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
