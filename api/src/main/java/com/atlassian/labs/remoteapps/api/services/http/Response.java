package com.atlassian.labs.remoteapps.api.services.http;

import java.io.InputStream;
import java.util.Map;

public class Response extends Message
{
    private int statusCode;
    private String statusText;

    public int getStatusCode()
    {
        return statusCode;
    }

    public Message setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
        return this;
    }

    public String getStatusText()
    {
        return statusText;
    }

    public Message setStatusText(String statusText)
    {
        this.statusText = statusText;
        return this;
    }

    @Override
    public Response setContentType(String contentType)
    {
        super.setContentType(contentType);
        return this;
    }

    @Override
    public Response setContentCharset(String contentCharset)
    {
        super.setContentCharset(contentCharset);
        return this;
    }

    @Override
    public Response setHeaders(Map<String, String> headers)
    {
        super.setHeaders(headers);
        return this;
    }

    @Override
    public Response setHeader(String name, String value)
    {
        super.setHeader(name, value);
        return this;
    }

    @Override
    public Response setEntity(String entity)
    {
        super.setEntity(entity);
        return this;
    }

    @Override
    public Response setEntityStream(InputStream entityStream, String encoding)
    {
        super.setEntityStream(entityStream, encoding);
        return this;
    }

    @Override
    public Response setEntityStream(InputStream entityStream)
    {
        super.setEntityStream(entityStream);
        return this;
    }
}
