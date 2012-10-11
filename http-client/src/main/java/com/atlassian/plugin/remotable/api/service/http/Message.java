package com.atlassian.plugin.remotable.api.service.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * An abstract base class for HTTP messages (i.e. Request and Response) with support for
 * header and entity management.
 */
public interface Message
{
    /**
     * Returns the IANA media type, minus charset information, for the current entity, if any.
     * To access charset information, use <code>getContentCharset()</code>.  To get the full
     * Content-Type header value including charset if specified, use <code>getHeader("Content-Type")</code>.
     *
     * @return An IANA media type, or null
     */
    String getContentType();

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
    Message setContentType(String contentType);

    /**
     * Returns the currently set content charset value, if any.
     *
     * @return The current content charset
     */
    String getContentCharset();

    /**
     * Sets the charset for this object's entity, if any.  This value is ignored during headeer access
     * if no entity is present or if the content type property is not set.
     *
     * @param contentCharset The entity's charset value, or null
     * @return This object, for builder-style chaining
     */
    Message setContentCharset(String contentCharset);

    /**
     * Returns the current entity as an input stream, or null if not set.  Use <code>hasEntity()</code>
     * to check if this message has an entity value.
     *
     * @return An input stream for the current entity, or null if not set
     * @throws IllegalStateException If the non-null entity has already been accessed once, through
     *         any accessor for this object
     */
    InputStream getEntityStream() throws IllegalStateException;

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
    Message setEntityStream(InputStream entityStream);

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
    Message setEntityStream(InputStream entityStream, String charset);

    /**
     * Returns the current entity in <code>String</code> form, if available, converting the underlying
     * entity stream to a string using the currently set content charset, or defaulting to the HTTP
     * standard of "ISO-8859-1" if no content charset has been specified.
     *
     * @return The entity string, or null if no entity has been set
     * @throws IOException If the conversion of the underlying entity stream to a string fails
     */
    String getEntity() throws IOException, IllegalStateException;

    /**
     * Sets this object's entity stream from a string.  Using this method of setting the entity
     * automatically sets this object's content charset property to "UTF-8" if the entity is not null.
     *
     * @param entity An entity string
     * @return This object, for builder-style chaining
     */
    Message setEntity(String entity);

    /**
     * Returns whether or not an entity has been set on this object.  Use this instead of calling
     * an entity getter to test for presence of an entity, as the getters will affect this object's
     * <code>hasReadEntity()</code> state.
     *
     * @return This object, for builder-style chaining
     */
    boolean hasEntity();

    /**
     * Returns whether or not the current entity property, if any, has been read from this object.
     * If this method returns true, any further calls to entity property accessors on this object
     * will result in an {@link IllegalStateException} being thrown.
     *
     * @return True if the entity has already been read
     */
    boolean hasReadEntity();

    /**
     * Returns a map of all headers that have been set on this object.  If the content type property
     * has been set, a full "Content-Type" header including content charset, if set, is generated as
     * part of this map.
     *
     * @return The headers map
     */
    Map<String, String> getHeaders();

    /**
     * Copies the specified map of HTTP headers into this object.  It will also parse any included
     * Content-Type header into its constituent parts of IANA media type and content charset, updating
     * those properties as appropriate.
     *
     * @param headers A map of HTTP headers
     * @return This object, for builder-style chaining
     */
    Message setHeaders(Map<String, String> headers);

    /**
     * Returns the specified header by name.  If "Content-Type" is requested, the value will be
     * constructed from this object's content type and content charset properties, if set and
     * as appropriate.
     *
     * @param name The name of the header to fetch
     * @return The value of the named header, or null if not set
     */
    String getHeader(String name);

    /**
     * Sets an HTTP header on this object.  If the header's name is "Content-Type", the value
     * will be parsed into this object's content type and content charset properties, as
     * appropriate.
     *
     * @param name The name of the header to be set
     * @param value The value of the header to be set
     * @return This object, for builder-style chaining
     */
    Message setHeader(String name, String value);

    /**
     * Returns whether or not this object has been made immutable.
     *
     * @return True if the object is immutable
     */
    boolean isFrozen();
}
