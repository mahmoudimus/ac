package com.atlassian.plugin.connect.plugin.module.permission;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Consumes the input stream for a request, allowing multiple executions
 */
public class InputConsumingHttpServletRequest extends HttpServletRequestWrapper
{
    private byte[] input = null;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request the HTTP servlet request
     * @throws IllegalArgumentException if the request is null
     */
    public InputConsumingHttpServletRequest(HttpServletRequest request)
    {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        lazilyLoadInput();
        final ByteArrayInputStream bin = new ByteArrayInputStream(input);
        return new ServletInputStream()
        {
            @Override
            public int read() throws IOException
            {
                return bin.read();
            }
        };
    }

    private void lazilyLoadInput() throws IOException
    {
        if (input == null)
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            IOUtils.copy(super.getInputStream(), bout);
            input = bout.toByteArray();
        }
    }

    @Override
    public BufferedReader getReader() throws IOException
    {
        lazilyLoadInput();
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input), "UTF-8"));
    }
}
