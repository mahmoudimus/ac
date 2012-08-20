package com.atlassian.labs.remoteapps.api.service.http;

import java.io.InputStream;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class Request extends Message
{
    public enum Method { GET, POST, PUT, DELETE }

    private Method method;
    private String uri;

    public Request()
    {
        setAccept("*/*");
    }

    public Request(String uri)
    {
        this(uri, null, null);
    }

    public Request(String uri, String contentType, String entity)
    {
        this();
        this.uri = uri;
        setContentType(contentType);
        setEntity(entity);
    }

    public Method getMethod()
    {
        return method;
    }

    public Request setMethod(Method method)
    {
        this.method = method;
        return this;
    }

    public Request setMethod(String method)
    {
        this.method = Method.valueOf(method);
        return this;
    }

    public String getUri()
    {
        return uri;
    }

    public Request setUri(String uri)
    {
        this.uri = uri;
        return this;
    }

    public String getAccept()
    {
        return getHeader("Accept");
    }

    public Request setAccept(String mediaType)
    {
        setHeader("Accept", mediaType);
        return this;
    }

    public Request validate()
    {
        super.validate();

        checkNotNull(uri);
        if (uri.matches("^[\\w]+:.*"))
        {
            throw new IllegalStateException("Absolute request URLs are not supported");
        }

        checkNotNull(method);
        if (method == Method.POST || method == Method.PUT)
        {
            if (!hasEntity())
            {
                throw new IllegalStateException("Request method " + method + " requires an entity stream");
            }
        }

        return this;
    }

    @Override
    public Request setContentType(String contentType)
    {
        super.setContentType(contentType);
        return this;
    }

    @Override
    public Request setContentCharset(String contentCharset)
    {
        super.setContentCharset(contentCharset);
        return this;
    }

    @Override
    public Request setHeaders(Map<String, String> headers)
    {
        super.setHeaders(headers);
        return this;
    }

    @Override
    public Request setHeader(String name, String value)
    {
        super.setHeader(name, value);
        return this;
    }

    @Override
    public Request setEntity(String entity)
    {
        super.setEntity(entity);
        return this;
    }

    @Override
    public Request setEntityStream(InputStream entityStream, String encoding)
    {
        super.setEntityStream(entityStream, encoding);
        return this;
    }

    @Override
    public Request setEntityStream(InputStream entityStream)
    {
        super.setEntityStream(entityStream);
        return this;
    }
}
