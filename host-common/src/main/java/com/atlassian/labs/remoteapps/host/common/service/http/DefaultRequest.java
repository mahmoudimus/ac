package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.service.http.ChainingFormBuilder;
import com.atlassian.labs.remoteapps.api.service.http.FormBuilder;
import com.atlassian.labs.remoteapps.api.service.http.Request;
import com.atlassian.labs.remoteapps.api.service.http.ResponsePromise;

import java.io.InputStream;
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
    private String uri;
    private Map<String, String> attributes;
    private boolean isFrozen;

    public DefaultRequest(AbstractHttpClient httpClient)
    {
        this.httpClient = httpClient;
        attributes = newHashMap();
        setAccept("*/*");
    }

    public DefaultRequest(AbstractHttpClient httpClient, String uri)
    {
        this(httpClient, uri, null, null);
    }

    public DefaultRequest(AbstractHttpClient httpClient, String uri, String contentType, String entity)
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
    public String getUri()
    {
        return uri;
    }

    @Override
    public Request setUri(String uri)
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
    public Request setAccept(String mediaType)
    {
        checkMutable();
        setHeader("Accept", mediaType);
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
    public Request setEntity(FormBuilder formBuilder)
    {
        setContentType(FormBuilder.CONTENT_TYPE);
        setEntity(formBuilder.toEntity());
        return this;
    }

    @Override
    public Request setFormEntity(FormBuilder formBuilder)
    {
        return setContentType(FormBuilder.CONTENT_TYPE).setEntity(formBuilder.toEntity());
    }

    @Override
    public Request setFormEntity(Map<String, String> params)
    {
        ChainingFormBuilder builder = buildFormEntity();
        for (Map.Entry<String, String> entry : params.entrySet())
        {
            String name = entry.getKey();
            String value = entry.getValue();
            builder.addParam(name, value);
        }
        builder.commit();
        return this;
    }

    @Override
    public Request setMultiValuedFormEntity(Map<String, List<String>> params)
    {
        ChainingFormBuilder builder = buildFormEntity();
        for (Map.Entry<String, List<String>> entry : params.entrySet())
        {
            String name = entry.getKey();
            List<String> values = entry.getValue();
            builder.setParam(name, values);
        }
        builder.commit();
        return this;
    }

    @Override
    public ChainingFormBuilder buildFormEntity()
    {
        return new DefaultChainingFormBuilder(this);
    }

    /**
     * Validates the state of this object, as follows:
     *
     *  - if an entity is present, that a content type has also been set (super)
     *  - the uri is not null
     *  - if the method is GET, DELETE, or HEAD, that no entity body is specified
     *  - if the method is POST, PUT, or TRACE, that an entity body is required
     *
     * @return This object, for builder-style chaining
     */
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

    public void freeze()
    {
        isFrozen = true;
    }

    @Override
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
