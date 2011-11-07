package com.atlassian.labs.remoteapps.modules.permissions;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 */
public class InputConsumingHttpServletRequest extends HttpServletRequestWrapper
{
    private byte[] input = null;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @throws IllegalArgumentException if the request is null
     */
    public InputConsumingHttpServletRequest(HttpServletRequest request)
    {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        if (input == null)
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            IOUtils.copy(super.getInputStream(), bout);
            input = bout.toByteArray();
        }
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
}
