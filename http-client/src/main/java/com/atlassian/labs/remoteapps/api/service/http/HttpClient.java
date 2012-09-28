package com.atlassian.labs.remoteapps.api.service.http;

import java.net.URI;

/**
 * A service providing asynchronous HTTP request creation and execution.
 *
 * To use this service, first create a {@link Request} instance with one of the <code>newRequest()</code>
 * methods.  Then, populate the request with any additional options, and finally call one of its HTTP verb
 * methods to execute the request.  See the {@link Request} class for finer control over the construction
 * of the HTTP requests to be executed.
 */
public interface HttpClient
{
    /**
     * Constructs a new request.  Sets the accept property to a default of "&#42;/&#42;".
     *
     * @return The new request object
     */
    Request newRequest();

    /**
     * Constructs a new Request with the specified URI.  Sets the accept property to a
     * default of "&#42;/&#42;".
     *
     * @param uri The endpoint URI for this request
     * @return The new request object
     */
    Request newRequest(URI uri);

    /**
     * Constructs a new Request with the specified URI.  Sets the accept property to a
     * default of "&#42;/&#42;".
     *
     * @param uri The endpoint URI for this request
     * @return The new request object
     */
    Request newRequest(String uri);

    /**
     * Constructs a new Request with the specified URI, contentType, and entity.  Sets the
     * accept property to a default of "&#42;/&#42;", and the content charset property to
     * "UTF-8".  This should only be used for sending textual content types, typically via
     * the POST or PUT HTTP methods.
     *
     * @param uri The endpoint URI for this request
     * @param contentType A textual IANA media type
     * @param entity A string entity to send as this request's message body
     * @return The new request object
     */
    Request newRequest(URI uri, String contentType, String entity);

    /**
     * Constructs a new Request with the specified URI, contentType, and entity.  Sets the
     * accept property to a default of "&#42;/&#42;", and the content charset property to
     * "UTF-8".  This should only be used for sending textual content types, typically via
     * the POST or PUT HTTP methods.
     *
     * @param uri The endpoint URI for this request
     * @param contentType A textual IANA media type
     * @param entity A string entity to send as this request's message body
     * @return The new request object
     */
    Request newRequest(String uri, String contentType, String entity);
}
