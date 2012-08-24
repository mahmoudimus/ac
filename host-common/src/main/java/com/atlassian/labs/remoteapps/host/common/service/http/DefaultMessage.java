package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.service.http.Message;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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
    private boolean hasRead;
    private Map<String, String> headers;

    /**
     * Constructs a new, empty DefaultMessage instance.
     */
    public DefaultMessage()
    {
        headers = newHashMap();
    }

    /**
     * Returns the IANA media type, minus charset information, for the current entity, if any.
     * To access charset information, use <code>getContentCharset()</code>.  To get the full
     * Content-Type header value including charset if specified, use <code>getHeader("Content-Type")</code>.
     *
     * @return An IANA media type, or null
     */
    public String getContentType()
    {
        return contentType;
    }

    /**
     * Sets the IANA media type, for the current entity, if any.  If the <code>contentType</code> argument
     * also contains charset information, this method will have the side effect of parsing the charset
     * out and storing the component parts independently.  The method <code>getContentCharset()</code> can
     * be used to retrieve extracted content charset, if present, and <code>getHeader("Content-Type")</code>
     * can be used to retrieve the entire Content-Type header, complete with charset information, if set.
     * The content type property is required when an entity is present.
     *
     * @param contentType An IANA media type with optional charset information
     * @return This object, for builder-style chaining
     */
    public Message setContentType(String contentType)
    {
        parseContentType(contentType);
        return this;
    }

    /**
     * Returns the currently set content charset value, if any.
     *
     * @return The current content charset
     */
    public String getContentCharset()
    {
        return contentCharset;
    }

    /**
     * Sets the charset for this object's entity, if any.  This value is ignored during headeer access
     * if no entity is present or if the content type property is not set.
     *
     * @param contentCharset The entity's charset value, or null
     * @return This object, for builder-style chaining
     */
    public Message setContentCharset(String contentCharset)
    {
        this.contentCharset = Charset.forName(contentCharset).name();
        return this;
    }

    /**
     * Returns the current entity as an input stream, or null if not set.  Use <code>hasEntity()</code>
     * to check if this message has an entity value.
     *
     * @return An input stream for the current entity, or null if not set
     * @throws IllegalStateException If the non-null entity has already been accessed once, through
     *         any accessor for this object
     */
    public InputStream getEntityStream() throws IllegalStateException
    {
        checkRead();
        return entityStream;
    }

    /**
     * Sets this object's entity as an input stream.  Invocations of this method reset this object's
     * <code>hasReadEntity()</code> state to <code>false</code>.  It is recommended to also set this
     * object's content charset property when setting an entity stream for a textual media type (or
     * using the overloaded form that takes both the entity stream and charset in the same call).
     * Clients of this object should assume the HTTP standard of <code>ISO-8859-1 (latin-1)</code>
     * for the content charset property if a textual media type is set but no explcit charset was
     * provided for this message.  A charset should NOT be provided for entity streams targetting
     * binary media types.
     *
     * @param entityStream An entity input stream ready to be read
     * @return This object, for builder-style chaining
     */
    public Message setEntityStream(InputStream entityStream)
    {
        this.entityStream = entityStream;
        hasRead = false;
        return this;
    }

    /**
     * Sets this object's entity as an input stream, encoded with the specified charset.  Invocations of
     * this method reset this object's <code>hasReadEntity()</code> state to <code>false</code>.  This
     * method should only be called for entity streams targetting textual media types -- that is, it's
     * nonsensical to set the charset of an entity stream for binary media types (e.g. image/*, etc).
     *
     * @param entityStream An entity input stream ready to be read
     * @param charset The charset in which the entity stream is encoded
     * @return This object, for builder-style chaining
     */
    public Message setEntityStream(InputStream entityStream, String charset)
    {
        setEntityStream(entityStream);
        setContentCharset(charset);
        return this;
    }

    /**
     * Returns the current entity in <code>String</code> form, if available, converting the underlying
     * entity stream to a string using the currently set content charset, or defaulting to the HTTP
     * standard of "ISO-8859-1" if no content charset has been specified.
     *
     * @return The entity string, or null if no entity has been set
     * @throws IOException If the conversion of the underlying entity stream to a string fails
     */
    public String getEntity() throws IOException, IllegalStateException
    {
        String entity = null;
        if (hasEntity())
        {
            String charset = getContentCharset();
            charset = charset != null ? charset : "ISO-8859-1";
            entity = IOUtils.toString(getEntityStream(), charset);
        }
        return entity;
    }

    /**
     * Sets this object's entity stream from a string.  Using this method of setting the entity
     * automatically sets this object's content charset property to "UTF-8" if the entity is not null.
     *
     * @param entity An entity string
     * @return This object, for builder-style chaining
     */
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

    /**
     * Returns whether or not an entity has been set on this object.  Use this instead of calling
     * an entity getter to test for presence of an entity, as the getters will affect this object's
     * <code>hasReadEntity()</code> state.
     *
     * @return This object, for builder-style chaining
     */
    public boolean hasEntity()
    {
        return entityStream != null;
    }

    /**
     * Returns whether or not the current entity property, if any, has been read from this object.
     * If this method returns true, any further calls to entity property accessors on this object
     * will result in an {@link IllegalStateException} being thrown.
     *
     * @return True if the entity has already been read
     */
    public boolean hasReadEntity()
    {
        return hasRead;
    }

    /**
     * Returns a map of all headers that have been set on this object.  If the content type property
     * has been set, a full "Content-Type" header including content charset, if set, is generated as
     * part of this map.
     *
     * @return The headers map
     */
    public Map<String, String> getHeaders()
    {
        Map<String,String> headers = newHashMap(this.headers);
        if (contentType != null)
        {
            headers.put("Content-Type", buildContentType());
        }
        return Collections.unmodifiableMap(headers);
    }

    /**
     * Copies the specified map of HTTP headers into this object.  It will also parse any included
     * Content-Type header into its constituent parts of IANA media type and content charset, updating
     * those properties as appropriate.
     *
     * @param headers A map of HTTP headers
     * @return This object, for builder-style chaining
     */
    public Message setHeaders(Map<String, String> headers)
    {
        headers.clear();
        for (Map.Entry<String,String> entry : headers.entrySet())
        {
            setHeader(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Returns the specified header by name.  If "Content-Type" is requested, the value will be
     * constructed from this object's content type and content charset properties, if set and
     * as appropriate.
     *
     * @param name The name of the header to fetch
     * @return The value of the named header, or null if not set
     */
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

    /**
     * Sets an HTTP header on this object.  If the header's name is "Content-Type", the value
     * will be parsed into this object's content type and content charset properties, as
     * appropriate.
     *
     * @param name The name of the header to be set
     * @param value The value of the header to be set
     * @return This object, for builder-style chaining
     */
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

    /**
     * Validates the state of this object, as follows:
     *
     *  - verifies that if an entity is present, that a content type has also been set
     *
     * @return This object, for builder-style chaining
     */
    public Message validate()
    {
        if (hasEntity() && contentType == null)
        {
            throw new IllegalStateException("Property contentType must be set when entity is present");
        }
        return this;
    }

    /**
     * Dumps a string representation of this object, including the entity.  Note that this is a potentially
     * expensive process, as it will consume the entity stream if it exists.  The enitity will still be accessible
     * after this function is run (and hasReadEntity() will still return false), but the result will still
     * be that the entity will have been loaded into memory, which may have non-tivial perofrmance impacts
     * in certain situations, so the use of this function is encouraged only as a debugging tool.
     *
     * @return An HTTP-formatted string representation of this object, including the value of its entity
     */
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
}
