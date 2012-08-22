package com.atlassian.labs.remoteapps.api.service.http;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public abstract class Message
{
    private String contentType;
    private String contentCharset;
    private InputStream entityStream;
    private boolean hasRead;
    private Map<String, String> headers;

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
        checkRead();
        return entityStream;
    }

    public Message setEntityStream(InputStream entityStream)
    {
        this.entityStream = entityStream;
        hasRead = false;
        return this;
    }

    public Message setEntityStream(InputStream entityStream, String charset)
    {
        setEntityStream(entityStream);
        setContentCharset(charset);
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
                String charset = "UTF-8";
                setEntityStream(IOUtils.toInputStream(entity, charset), charset);
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
                setEntity(entity);
            }
            catch (IOException e)
            {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                buf.append("ERROR: failed to read request entity").append(lf).append(writer.toString());
            }
        }
        return buf.toString();
    }

    private void checkRead()
    {
        if (hasRead)
        {
            throw new IllegalStateException("Entity may only be accessed once");
        }
        hasRead = true;
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
                    contentCharset = Charset.forName(charset.substring(8)).name();
                }
            }
        }
        else
        {
            contentType = null;
        }
    }
}
