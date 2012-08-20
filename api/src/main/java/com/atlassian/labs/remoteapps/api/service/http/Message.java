package com.atlassian.labs.remoteapps.api.service.http;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public abstract class Message
{
    private String contentType;
    private String contentCharset;
    private InputStream entityStream;
    private boolean hasRead;
    private Map<String,String> headers;

    public Message()
    {
        headers = newHashMap();
    }

    public String getContentType()
    {
        return contentType;
    }

    public Message setContentType(String contentType)
    {
        this.contentType = contentType;
        return this;
    }

    public String getContentCharset()
    {
        return contentCharset;
    }

    public Message setContentCharset(String contentCharset)
    {
        this.contentCharset = contentCharset;
        return this;
    }

    public InputStream getEntityStream()
    {
        preRead();
        return entityStream;
    }

    public Message setEntityStream(InputStream entityStream)
    {
        this.entityStream = entityStream;
        hasRead = false;
        return this;
    }

    public Message setEntityStream(InputStream entityStream, String encoding)
    {
        setEntityStream(entityStream);
        setContentCharset(encoding);
        return this;
    }

    public String getEntity()
        throws IOException
    {
        return IOUtils.toString(getEntityStream(), getContentCharset());
    }

    public Message setEntity(String entity)
    {
        if (entity != null)
        {
            try
            {
                String encoding = "UTF-8";
                setEntityStream(IOUtils.toInputStream(entity, encoding), encoding);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    public boolean hasEntity()
    {
        return entityStream != null;
    }

    public Map<String,String> getHeaders()
    {
        Map<String,String> headers = newHashMap(this.headers);
        if (contentType != null)
        {
            headers.put("Content-Type", getHeader("Content-Type"));
        }
        return headers;
    }

    public Message setHeaders(Map<String,String> headers)
    {
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
            value = contentType != null ? contentType : "text/plain";
            if (contentCharset != null)
            {
                value += "; charset=" + contentCharset;
            }
        }
        else
        {
            value = headers.get(name);
        }
        return value;
    }

    public Message setHeader(String name, String value)
    {
        if (name.equalsIgnoreCase("Content-Type"))
        {
            String[] parts = value.split(";");
            if (parts.length >= 1)
            {
                contentType = parts[0].trim();
            }
            if (parts.length >= 2)
            {
                String encoding = parts[1].trim();
                if (parts[1].startsWith("charset="))
                {
                    contentCharset = Charset.forName(encoding.substring(8)).name();
                }
            }
            else
            {
                contentCharset = null;
            }
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

    private void preRead()
    {
        if (hasRead)
        {
            throw new IllegalStateException("Entity may only be accessed once.");
        }
        hasRead = true;
    }
}
