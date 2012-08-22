package com.atlassian.labs.remoteapps.api.service.http;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.api.PromiseCallback;
import com.google.common.util.concurrent.FutureCallback;

/**
 * Adds rich response-code callback registration to the base Promise interface.
 */
public interface ResponsePromise extends Promise<Response>
{
    /**
     * Register a callback to respond to 'informational' (1xx) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise informational(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to 'successful' (2xx) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise successful(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to 'ok' (200) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise ok(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to 'created' (201) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise created(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to 'no content' (204) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise noContent(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to 'redirection' (3xx) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise redirection(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to 'see other' (303) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise seeOther(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to 'not modified' (304) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise notModified(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to 'client error' (4xx) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise clientError(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to 'bad request' (400) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise badRequest(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to 'unauthorized' (401) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise unauthorized(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to 'forbidden' (403) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise forbidden(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to 'conflict' (409) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise conflict(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to 'server error' (5xx) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise serverError(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to 'internal server error' (500) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise internalServerError(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to 'service unavailable' (503) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise serviceUnavailable(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to all error (4xx and 5xx) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise error(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to all non-'successful' (1xx, 3xx, 4xx, 5xx) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise notSuccessful(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to all other HTTP responses (i.e. those not explcitly registered for).
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    ResponsePromise others(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to all completed (1xx, 2xx, 3xx, 4xx, and 5xx) HTTP responses.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    @Override
    ResponsePromise done(PromiseCallback<Response> callback);

    /**
     * Register a callback to respond to exceptions thrown while executing the HTTP request.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    @Override
    ResponsePromise fail(PromiseCallback<Throwable> callback);

    /**
     * Register a future callback to respond to all completed HTTP responses and exceptions thrown
     * while executing the HTTP request.
     *
     * @param callback The callback
     * @return This ResponsePromise instance for chaining
     */
    @Override
    ResponsePromise then(FutureCallback<Response> callback);
}
