package com.atlassian.plugin.remotable.host.common.service.http;

import com.atlassian.plugin.remotable.api.service.http.Message;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import static com.google.common.collect.Maps.*;

/**
 * An abstract base class for HTTP messages (i.e. Request and Response) with support for
 * header and entity management.
 */
public abstract class DefaultMessage implements Message
{
    private String contentType;
    private String contentCharset;
    private InputStream entityStream;
    private boolean hasRead;
    private Map<String, String> headers;
    private boolean isFrozen;

    public DefaultMessage()
    {
        headers = newHashMap();
    }

    public String getContentType()
    {
        return contentType;
    }

    public Message setContentType(String contentType)
    {
        checkMutable();
        parseContentType(contentType);
        return this;
    }

    public String getContentCharset()
    {
        return contentCharset;
    }

    public Message setContentCharset(String contentCharset)
    {
        checkMutable();
        this.contentCharset = contentCharset != null ? Charset.forName(contentCharset).name() : null;
        return this;
    }

    public InputStream getEntityStream() throws IllegalStateException
    {
        checkRead();
        return entityStream;
    }

    public Message setEntityStream(InputStream entityStream)
    {
        checkMutable();
        this.entityStream = entityStream;
        hasRead = false;
        return this;
    }

    public Message setEntityStream(InputStream entityStream, String charset)
    {
        checkMutable();
        setEntityStream(entityStream);
        setContentCharset(charset);
        return this;
    }

    public String getEntity() throws IOException, IllegalStateException
    {
        String entity = null;
        if (hasEntity())
        {
            final String charsetAsString = getContentCharset();
            // TODO: ISO-8859-1 by default really?
            final Charset charset = charsetAsString != null ? Charset.forName(charsetAsString) : Charset.forName("ISO-8859-1");
            entity = CharStreams.toString(CharStreams.newReaderSupplier(new InputSupplier<InputStream>()
            {
                @Override
                public InputStream getInput() throws IOException
                {
                    return getEntityStream();
                }
            }, charset));
        }
        return entity;
    }

    public Message setEntity(String entity)
    {
        checkMutable();
        if (entity != null)
        {
            final String charset = "UTF-8";
            byte[] bytes = entity.getBytes(Charset.forName(charset));
            setEntityStream(new EntityByteArrayInputStream(bytes), charset);
        }
        else
        {
            setEntityStream(null, null);
        }
        return this;
    }

    public boolean hasEntity()
    {
        return entityStream != null;
    }

    HttpEntity getHttpEntity()
    {
        HttpEntity entity = null;
        if (hasEntity())
        {
            if (entityStream instanceof ByteArrayInputStream)
            {
                byte[] bytes;
                if (entityStream instanceof EntityByteArrayInputStream)
                {
                    bytes = ((EntityByteArrayInputStream) entityStream).getBytes();
                }
                else
                {
                    try
                    {
                        bytes = ByteStreams.toByteArray(entityStream);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
                entity = new ByteArrayEntity(bytes);
            }
            else
            {
                entity = new InputStreamEntity(entityStream, -1);
            }
        }
        return entity;
    }

    public boolean hasReadEntity()
    {
        return hasRead;
    }

    public Map<String, String> getHeaders()
    {
        Map<String,String> headers = newHashMap(this.headers);
        if (contentType != null)
        {
            headers.put("Content-Type", buildContentType());
        }
        return Collections.unmodifiableMap(headers);
    }

    public Message setHeaders(Map<String, String> headers)
    {
        checkMutable();
        this.headers.clear();
        for (Map.Entry<String,String> entry : headers.entrySet())
        {
            setHeader(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public String getHeader(String name)
    {
        String value;
        if (name.equalsIgnoreCase("Content-Type"))
        {
            value = buildContentType();
        }
        else
        {
            value = headers.get(name);
        }
        return value;
    }

    public Message setHeader(String name, String value)
    {
        checkMutable();
        if (name.equalsIgnoreCase("Content-Type"))
        {
            parseContentType(value);
        }
        else
        {
            headers.put(name, value);
        }
        return this;
    }

    public Message validate()
    {
        if (hasEntity() && contentType == null)
        {
            throw new IllegalStateException("Property contentType must be set when entity is present");
        }
        return this;
    }

    private void checkRead() throws IllegalStateException
    {
        if (entityStream != null)
        {
            if (hasRead)
            {
                throw new IllegalStateException("Entity may only be accessed once");
            }
            hasRead = true;
        }
    }

    private String buildContentType()
    {
        String value = contentType != null ? contentType : "text/plain";
        if (contentCharset != null)
        {
            value += "; charset=" + contentCharset;
        }
        return value;
    }

    private void parseContentType(String value)
    {
        if (value != null)
        {
            String[] parts = value.split(";");
            if (parts.length >= 1)
            {
                contentType = parts[0].trim();
            }
            if (parts.length >= 2)
            {
                String charset = parts[1].trim();
                if (parts[1].startsWith("charset="))
                {
                    setContentCharset(charset.substring(8));
                }
            }
        }
        else
        {
            contentType = null;
        }
    }

    @Override
    public boolean isFrozen()
    {
        return isFrozen;
    }

    protected Message freeze()
    {
        isFrozen = true;
        return this;
    }

    protected void checkMutable()
    {
        if (isFrozen)
        {
            throw new IllegalStateException("Object cannot be changed once frozen");
        }
    }
}
