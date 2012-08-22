package com.atlassian.labs.remoteapps.api.service.http;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

public class Request extends Message
{
    public enum Method { GET, POST, PUT, DELETE }

    private Method method;
    private String uri;
    private Map<String, String> attributes;
    private boolean isFrozen;

    public Request()
    {
        attributes = newHashMap();
        setAccept("*/*");
    }

    public Request(String uri)
    {
        this(uri, null, null);
    }

    public Request(String uri, String contentType, String entity)
    {
        this();
        setUri(uri).setContentType(contentType).setEntity(entity);
    }

    public Method getMethod()
    {
        return method;
    }

    public Request setMethod(Method method)
    {
        checkMutable();
        this.method = method;
        return this;
    }

    public Request setMethod(String method)
    {
        return setMethod(Method.valueOf(method));
    }

    public String getUri()
    {
        return uri;
    }

    public Request setUri(String uri)
    {
        checkMutable();
        this.uri = uri;
        return this;
    }

    public String getAccept()
    {
        return getHeader("Accept");
    }

    public Request setAccept(String mediaType)
    {
        checkMutable();
        setHeader("Accept", mediaType);
        return this;
    }

    public Request setAttribute(String name, String value)
    {
        checkMutable();
        attributes.put(name, value);
        return this;
    }

    public String getAttribute(String name)
    {
        return attributes.get(name);
    }

    public Map<String, String> getAttributes()
    {
        return Collections.unmodifiableMap(attributes);
    }

    public Request setEntity(FormBuilder formBuilder)
    {
        setContentType(FormBuilder.CONTENT_TYPE);
        setEntity(formBuilder.toEntity());
        return this;
    }

    public Request validate()
    {
        super.validate();

        checkNotNull(uri);

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

    public void freeze()
    {
        isFrozen = true;
    }

    public boolean isFrozen()
    {
        return isFrozen;
    }

    @Override
    public Request setContentType(String contentType)
    {
        checkMutable();
        super.setContentType(contentType);
        return this;
    }

    @Override
    public Request setContentCharset(String contentCharset)
    {
        checkMutable();
        super.setContentCharset(contentCharset);
        return this;
    }

    @Override
    public Request setHeaders(Map<String, String> headers)
    {
        checkMutable();
        super.setHeaders(headers);
        return this;
    }

    @Override
    public Request setHeader(String name, String value)
    {
        checkMutable();
        super.setHeader(name, value);
        return this;
    }

    @Override
    public Request setEntity(String entity)
    {
        checkMutable();
        super.setEntity(entity);
        return this;
    }

    @Override
    public Request setEntityStream(InputStream entityStream, String encoding)
    {
        checkMutable();
        super.setEntityStream(entityStream, encoding);
        return this;
    }

    @Override
    public Request setEntityStream(InputStream entityStream)
    {
        checkMutable();
        super.setEntityStream(entityStream);
        return this;
    }

    @Override
    public String dump()
    {
        StringBuilder buf = new StringBuilder();
        String lf = System.getProperty("line.separator");
        buf.append(method).append(" ").append(getUri()).append(" HTTP/1.1").append(lf);
        buf.append(super.dump());
        return buf.toString();
    }

    private void checkMutable()
    {
        if (isFrozen)
        {
            throw new IllegalStateException("Request cannot be changed once frozen");
        }
    }
}
