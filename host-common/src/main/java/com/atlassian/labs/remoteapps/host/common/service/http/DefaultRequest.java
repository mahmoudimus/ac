package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.service.http.*;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

public class DefaultRequest extends DefaultMessage implements Request
{
    public enum Method { GET, POST, PUT, DELETE, OPTIONS, HEAD, TRACE }

    private AbstractHttpClient httpClient;
    private Method method;
    private URI uri;
    private Map<String, String> attributes;

    public DefaultRequest(AbstractHttpClient httpClient)
    {
        this.httpClient = httpClient;
        attributes = newHashMap();
        setAccept("*/*");
    }

    public DefaultRequest(AbstractHttpClient httpClient, URI uri)
    {
        this(httpClient, uri, null, null);
    }

    public DefaultRequest(AbstractHttpClient httpClient, URI uri, String contentType, String entity)
    {
        this(httpClient);
        setUri(uri).setContentType(contentType).setEntity(entity);
    }

    @Override
    public ResponsePromise get()
    {
        setMethod(Method.GET);
        return httpClient.execute(this);
    }

    @Override
    public ResponsePromise post()
    {
        setMethod(Method.POST);
        return httpClient.execute(this);
    }

    @Override
    public ResponsePromise put()
    {
        setMethod(Method.PUT);
        return httpClient.execute(this);
    }

    @Override
    public ResponsePromise delete()
    {
        setMethod(Method.DELETE);
        return httpClient.execute(this);
    }

    @Override
    public ResponsePromise options()
    {
        setMethod(Method.OPTIONS);
        return httpClient.execute(this);
    }

    @Override
    public ResponsePromise head()
    {
        setMethod(Method.HEAD);
        return httpClient.execute(this);
    }

    @Override
    public ResponsePromise trace()
    {
        setMethod(Method.TRACE);
        return httpClient.execute(this);
    }

    public Method getMethod()
    {
        return method;
    }

    private Request setMethod(Method method)
    {
        checkMutable();
        this.method = method;
        return this;
    }

    @Override
    public URI getUri()
    {
        return uri;
    }

    @Override
    public Request setUri(URI uri)
    {
        checkMutable();
        this.uri = uri;
        return this;
    }

    @Override
    public String getAccept()
    {
        return getHeader("Accept");
    }

    @Override
    public Request setAccept(String accept)
    {
        checkMutable();
        setHeader("Accept", accept);
        return this;
    }

    @Override
    public Request setAttribute(String name, String value)
    {
        checkMutable();
        attributes.put(name, value);
        return this;
    }

    @Override
    public String getAttribute(String name)
    {
        return attributes.get(name);
    }

    @Override
    public Map<String, String> getAttributes()
    {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public Request setEntity(EntityBuilder entityBuilder)
    {
        return setHeaders(entityBuilder.getHeaders()).setEntityStream(entityBuilder.build());
    }

    public Request validate()
    {
        super.validate();
        checkNotNull(uri);
        checkNotNull(method);

        switch (method)
        {
            case GET:
            case DELETE:
            case HEAD:
                if (hasEntity())
                {
                    throw new IllegalStateException("Request method " + method + " doesn not support an entity");
                }
                break;
            case POST:
            case PUT:
            case TRACE:
                if (!hasEntity())
                {
                    throw new IllegalStateException("Request method " + method + " requires an entity");
                }
                break;
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

    @Override
    public String dump()
    {
        StringBuilder buf = new StringBuilder();
        String lf = System.getProperty("line.separator");
        buf.append(method).append(" ").append(getUri()).append(" HTTP/1.1").append(lf);
        buf.append(super.dump());
        return buf.toString();
    }

    @Override
    protected Request freeze()
    {
        super.freeze();
        return this;
    }
}
