package com.atlassian.labs.remoteapps.api.service.http;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

/**
 * An interface for building and executing HTTP requests.
 */
public interface Request extends Message
{
    /**
     * Returns this request's URI, if set.
     *
     * @return The URI or null if not yet set
     */
    URI getUri();

    /**
     * Sets this request's URI.  Must not be null by the time the request is executed.
     *
     * @param uri The URI
     * @return This object, for builder-style chaining
     */
    Request setUri(URI uri);

    /**
     * Returns this request's Accept header, if set.
     *
     * @return The accept header value
     */
    String getAccept();

    /**
     * Sets the Accept header for the request.
     *
     * @param accept An accept header expression containing media types, ranges, and/or quality factors
     * @return This object, for builder-style chaining
     */
    Request setAccept(String accept);

    /**
     * Sets an attribute on the request.  Attributes are request metadata that are forwarded to the
     * analytics plugin when enabled.
     *
     * @param name The attribute name
     * @param value The attribute value
     * @return This object, for builder-style chaining
     */
    Request setAttribute(String name, String value);

    /**
     * Gets an attribute from the request.  Attributes are request metadata that are forwarded to the
     * analytics plugin when enabled.
     *
     * @param name The attribute name
     * @return The attribute value, or null if not set
     */
    String getAttribute(String name);

    /**
     * Gets all attributes for this request.  Attributes are request metadata that are forwarded to the
     * analytics plugin when enabled.
     *
     * @return All attributes
     */
    Map<String, String> getAttributes();

    /**
     * Sets the entity and any associated headers from an entity builder.
     *
     * @param entityBuilder An entity builder
     * @return This object, for builder-style chaining
     */
    Request setEntity(EntityBuilder entityBuilder);

    /**
     * Executes this request through the {@link HttpClient} service as a <code>GET</code> operation.
     * The request SHOULD NOT contain an entity for the <code>GET</code> operation.
     *
     * @return A promise object that can be used to receive the response and handle exceptions
     */
    ResponsePromise get();

    /**
     * Executes this request through the {@link HttpClient} service as a <code>POST</code> operation.
     * The request SHOULD contain an entity for the <code>POST</code> operation.
     *
     * @return A promise object that can be used to receive the response and handle exceptions
     */
    ResponsePromise post();

    /**
     * Executes this request through the {@link HttpClient} service as a <code>PUT</code> operation.
     * The request SHOULD contain an entity for the <code>PUT</code> operation.
     *
     * @return A promise object that can be used to receive the response and handle exceptions
     */
    ResponsePromise put();

    /**
     * Executes this request through the {@link HttpClient} service as a <code>DELETE</code> operation.
     * The request SHOULD NOT contain an entity for the <code>DELETE</code> operation.
     *
     * @return A promise object that can be used to receive the response and handle exceptions
     */
    ResponsePromise delete();

    /**
     * Executes this request through the {@link HttpClient} service as a <code>OPTIONS</code> operation.
     * The request MAY contain an entity for the <code>OPTIONS</code> operation.
     *
     * @return A promise object that can be used to receive the response and handle exceptions
     */
    ResponsePromise options();

    /**
     * Executes this request through the {@link HttpClient} service as a <code>HEAD</code> operation.
     * The request SHOULD NOT contain an entity for the <code>HEAD</code> operation.
     *
     * @return A promise object that can be used to receive the response and handle exceptions
     */
    ResponsePromise head();

    /**
     * Executes this request through the {@link HttpClient} service as a <code>TRACE</code> operation.
     * The request SHOULD contain an entity for the <code>TRACE</code> operation.
     *
     * @return A promise object that can be used to receive the response and handle exceptions
     */
    ResponsePromise trace();

    @Override
    String dump();

    @Override
    Request setContentType(String contentType);

    @Override
    Request setContentCharset(String contentCharset);

    @Override
    Request setHeaders(Map<String, String> headers);

    @Override
    Request setHeader(String name, String value);

    @Override
    Request setEntity(String entity);

    @Override
    Request setEntityStream(InputStream entityStream, String encoding);

    @Override
    Request setEntityStream(InputStream entityStream);
}
