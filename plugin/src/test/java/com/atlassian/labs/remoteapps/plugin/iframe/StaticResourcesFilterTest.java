package com.atlassian.labs.remoteapps.plugin.iframe;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class StaticResourcesFilterTest
{
    private static final String JS_FILE = "all.js";
    private static final String OTHER_JS_FILE = "other.js";
    private static final byte[] JS_DATA = "/* some js */".getBytes(Charset.forName("UTF-8"));

    private Filter filter;

    @Before
    public void before() throws ServletException
    {
        PluginRetrievalService pluginRetrievalService = mock(PluginRetrievalService.class);
        Plugin plugin = mock(Plugin.class);
        when(pluginRetrievalService.getPlugin()).thenReturn(plugin);
        when(plugin.getResourceAsStream(JS_FILE)).thenReturn(new ByteArrayInputStream(JS_DATA));
        when(plugin.getResourceAsStream(OTHER_JS_FILE)).thenReturn(new ByteArrayInputStream(JS_DATA));

        FilterConfig config = mock(FilterConfig.class);
        ServletContext context = mock(ServletContext.class);
        when(config.getServletContext()).thenReturn(context);
        when(context.getMimeType(JS_FILE)).thenReturn("text/javascript");

        filter = new StaticResourcesFilter(pluginRetrievalService);
        filter.init(config);
    }

    @Test
    public void testNoGzip() throws IOException
    {
        HttpServletRequest request = mockRequest(JS_FILE);
        when(request.getHeader("Accept-Encoding")).thenReturn("");
        ServletOutputStream sos = mock(ServletOutputStream.class);
        HttpServletResponse response = mockResponse(sos);
        FilterChain chain = mock(FilterChain.class);
        try
        {
            filter.doFilter(request, response, chain);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        byte [] jsBytes = JS_DATA;
        verify(response).setHeader("Content-Encoding", "identity");
        verify(response).setStatus(200);
        verify(response).setContentLength(jsBytes.length);
        verify(response).setContentType("text/javascript");
        verify(response).setHeader("ETag", etag(jsBytes));
        verify(response).setHeader("Vary", "Accept-Encoding");
        verify(response).setHeader("Connection", "keep-alive");
        verify(sos).write(jsBytes);
    }

    @Test
    public void testGzip() throws IOException
    {
        HttpServletRequest request = mockRequest(JS_FILE);
        when(request.getHeader("Accept-Encoding")).thenReturn("gzip");
        ServletOutputStream sos = mock(ServletOutputStream.class);
        HttpServletResponse response = mockResponse(sos);
        FilterChain chain = mock(FilterChain.class);
        try
        {
            filter.doFilter(request, response, chain);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        byte [] jsBytes = gzip(JS_DATA);
        verify(response).setHeader("Content-Encoding", "gzip");
        verify(response).setStatus(200);
        verify(response).setContentLength(jsBytes.length);
        verify(response).setContentType("text/javascript");
        verify(response).setHeader("ETag", etag(jsBytes));
        verify(response).setHeader("Vary", "Accept-Encoding");
        verify(response).setHeader("Connection", "keep-alive");
        verify(sos).write(jsBytes);
    }

    @Test
    public void testNotModified() throws IOException
    {
        HttpServletRequest request = mockRequest(JS_FILE);
        when(request.getHeader("Accept-Encoding")).thenReturn("gzip");
        when(request.getHeader("If-None-Match")).thenReturn("4a3b8c014d4776ad51fb57b4bebe4f20");
        ServletOutputStream sos = mock(ServletOutputStream.class);
        HttpServletResponse response = mockResponse(sos);
        FilterChain chain = mock(FilterChain.class);
        try
        {
            filter.doFilter(request, response, chain);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        byte [] jsBytes = gzip(JS_DATA);
        verify(response, never()).setHeader(eq("Content-Encoding"), anyString());
        verify(response).setStatus(304);
        verify(response, never()).setContentLength(anyInt());
        verify(response).setContentType("text/javascript");
        verify(response).setHeader("ETag", etag(jsBytes));
        verify(response).setHeader("Vary", "Accept-Encoding");
        verify(response).setHeader("Connection", "keep-alive");
        verify(sos, never()).write((byte[]) anyObject());
    }

    @Test
    public void testModified() throws IOException
    {
        HttpServletRequest request = mockRequest(JS_FILE);
        when(request.getHeader("Accept-Encoding")).thenReturn("gzip");
        when(request.getHeader("If-None-Match")).thenReturn("stale-value");
        ServletOutputStream sos = mock(ServletOutputStream.class);
        HttpServletResponse response = mockResponse(sos);
        FilterChain chain = mock(FilterChain.class);
        try
        {
            filter.doFilter(request, response, chain);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        byte [] jsBytes = gzip(JS_DATA);
        verify(response).setHeader("Content-Encoding", "gzip");
        verify(response).setStatus(200);
        verify(response).setContentLength(jsBytes.length);
        verify(response).setContentType("text/javascript");
        verify(response).setHeader("ETag", etag(jsBytes));
        verify(response).setHeader("Vary", "Accept-Encoding");
        verify(response).setHeader("Connection", "keep-alive");
        verify(sos).write(jsBytes);
    }

    @Test
    public void testNotFoundExisting() throws IOException
    {
        // plugin actually knows about other.js, but the filter pattern should hide it
        HttpServletRequest request = mockRequest(OTHER_JS_FILE);
        when(request.getHeader("Accept-Encoding")).thenReturn("gzip");
        ServletOutputStream sos = mock(ServletOutputStream.class);
        HttpServletResponse response = mockResponse(sos);
        FilterChain chain = mock(FilterChain.class);
        try
        {
            filter.doFilter(request, response, chain);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        verify(response, never()).setHeader(eq("Content-Encoding"), anyString());
        verify(response).sendError(eq(404), anyString());
        verify(response, never()).setContentLength(anyInt());
        verify(response, never()).setContentType(anyString());
        verify(response, never()).setHeader(eq("ETag"), anyString());
        verify(response, never()).setHeader(eq("Vary"), anyString());
        verify(response, never()).setHeader(eq("Connection"), anyString());
        verify(sos, never()).write((byte[]) anyObject());
    }

    @Test
    public void testNotFoundMissing() throws IOException
    {
        // plugin doesn't know about other2.js
        HttpServletRequest request = mockRequest("other2.js");
        when(request.getHeader("Accept-Encoding")).thenReturn("gzip");
        ServletOutputStream sos = mock(ServletOutputStream.class);
        HttpServletResponse response = mockResponse(sos);
        FilterChain chain = mock(FilterChain.class);
        try
        {
            filter.doFilter(request, response, chain);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
        verify(response, never()).setHeader(eq("Content-Encoding"), anyString());
        verify(response).sendError(eq(404), anyString());
        verify(response, never()).setContentLength(anyInt());
        verify(response, never()).setContentType(anyString());
        verify(response, never()).setHeader(eq("ETag"), anyString());
        verify(response, never()).setHeader(eq("Vary"), anyString());
        verify(response, never()).setHeader(eq("Connection"), anyString());
        verify(sos, never()).write((byte[]) anyObject());
    }

    private HttpServletRequest mockRequest(String localPath)
    {
        String contextPath = "/foo";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(contextPath + "/remoteapps/" + localPath);
        when(request.getContextPath()).thenReturn(contextPath);
        return request;
    }

    private HttpServletResponse mockResponse(ServletOutputStream sos) throws IOException
    {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(sos);
        return response;
    }

    private byte[] gzip(byte[] data) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        GZIPOutputStream out = new GZIPOutputStream(bytes);
        IOUtils.copy(new ByteArrayInputStream(data), out);
        out.finish();
        out.close();
        return bytes.toByteArray();
    }

    private String etag(byte[] data)
    {
        return DigestUtils.md5Hex(data);
    }
}
