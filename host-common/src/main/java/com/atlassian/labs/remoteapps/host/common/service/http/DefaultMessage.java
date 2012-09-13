package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.service.http.Message;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * An abstract base class for HTTP messages (i.e. Request and Response) with support for
 * header and entity management.
 */
public abstract class DefaultMessage implements Message
{
    private String contentType;
    private String contentCharset;
    private InputStream entityStream;
    private byte[] entityBytes;
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
            String charset = getContentCharset();
            charset = charset != null ? charset : "ISO-8859-1";
            if (entityStream != null)
            {
                entity = IOUtils.toString(getEntityStream(), charset);
            }
            else
            {
                entity = new String(entityBytes);
            }
        }
        return entity;
    }

    byte[] getEntityBytes()
    {
        return entityBytes;
    }

    public Message setEntity(String entity)
    {
        checkMutable();
        if (entity != null)
        {
            try
            {
                final String charset = "UTF-8";
                entityBytes = entity.getBytes(Charset.forName(charset));
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        else {
            entityBytes = null;
        }
        return this;
    }

    public boolean hasEntity()
    {
        return entityStream != null || entityBytes != null;
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
        headers.clear();
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

    public String dump()
    {
        StringBuilder buf = new StringBuilder();
        String lf = System.getProperty("line.separator");
        for (Map.Entry<String, String> header : getHeaders().entrySet())
        {
            buf.append(header.getKey()).append(": ").append(header.getValue()).append(lf);
        }
        if (hasEntity())
        {
            buf.append(lf);
            try
            {
                String entity = getEntity();
                buf.append(entity).append(lf);
                // hack to get around hasRead guard for streaming entities, which might cause perf issues
                // fixme: this will break binary responses
                setEntityStream(new ByteArrayInputStream(entity.getBytes()));
            }
            catch (IOException e)
            {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                buf.append("ERROR: failed to read entity").append(lf).append(writer.toString());
            }
        }
        return buf.toString();
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
