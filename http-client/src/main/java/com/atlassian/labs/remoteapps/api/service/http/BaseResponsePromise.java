package com.atlassian.labs.remoteapps.api.service.http;

import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promise;
import com.google.common.util.concurrent.FutureCallback;

/**
 * Adds rich response-code callback registration to the base Promise interface.
 */
public interface BaseResponsePromise<V> extends Promise<V>
{
    // Custom Selectors

    /**
     * Register a callback to respond to HTTP responses with a specific status code.
     * Use this as a fallback if the status code you're interested in does not have
     * a more explicit registration method for it.
     *
     * @param statusCode The code to select on
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> on(int statusCode, Effect<V> callback);

    // Informational (1xx) Selectors

    /**
     * Register a callback to respond to 'informational' (1xx) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> informational(Effect<V> callback);

    // Successful (2xx) Selectors

    /**
     * Register a callback to respond to 'successful' (2xx) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> successful(Effect<V> callback);

    /**
     * Register a callback to respond to 'ok' (200) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> ok(Effect<V> callback);

    /**
     * Register a callback to respond to 'created' (201) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> created(Effect<V> callback);

    /**
     * Register a callback to respond to 'no content' (204) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> noContent(Effect<V> callback);

    // Redirection (3xx) Selectors

    /**
     * Register a callback to respond to 'redirection' (3xx) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> redirection(Effect<V> callback);

    /**
     * Register a callback to respond to 'see other' (303) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> seeOther(Effect<V> callback);

    /**
     * Register a callback to respond to 'not modified' (304) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> notModified(Effect<V> callback);

    // Client Error (4xx) Selectors

    /**
     * Register a callback to respond to 'client error' (4xx) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> clientError(Effect<V> callback);

    /**
     * Register a callback to respond to 'bad request' (400) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> badRequest(Effect<V> callback);

    /**
     * Register a callback to respond to 'unauthorized' (401) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> unauthorized(Effect<V> callback);

    /**
     * Register a callback to respond to 'forbidden' (403) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> forbidden(Effect<V> callback);

    /**
     * Register a callback to respond to 'not found' (404) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> notFound(Effect<V> callback);

    /**
     * Register a callback to respond to 'conflict' (409) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> conflict(Effect<V> callback);

    // Server Error (5xx) Selectors

    /**
     * Register a callback to respond to 'server error' (5xx) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> serverError(Effect<V> callback);

    /**
     * Register a callback to respond to 'internal server error' (500) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> internalServerError(Effect<V> callback);

    /**
     * Register a callback to respond to 'service unavailable' (503) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> serviceUnavailable(Effect<V> callback);

    // Aggregate Selectors

    /**
     * Register a callback to respond to all error (4xx and 5xx) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> error(Effect<V> callback);

    /**
     * Register a callback to respond to all non-'successful' (1xx, 3xx, 4xx, 5xx) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> notSuccessful(Effect<V> callback);

    /**
     * Register a callback to respond to all other HTTP responses (i.e. those not explcitly registered for).
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> others(Effect<V> callback);

    /**
     * Registers the specified callback as a handler for both of the following events:
     * <ul>
     *     <li>Any value passed to <code>fail()</code></li>
     *     <li>Any value passed to others(), converted into an exception</li>
     * </ul>
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    BaseResponsePromise<V> otherwise(Effect<Throwable> callback);

    /**
     * Register a callback to respond to all completed (1xx, 2xx, 3xx, 4xx, and 5xx) HTTP responses.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    @Override
    BaseResponsePromise<V> onSuccess(Effect<V> callback);

    // Exception Selectors

    /**
     * Register a callback to respond to exceptions thrown while executing the HTTP request.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    @Override
    BaseResponsePromise<V> onFailure(Effect<Throwable> callback);

    // Universal Selectors

    /**
     * Register a future callback to respond to all completed HTTP responses and exceptions thrown
     * while executing the HTTP request.
     *
     * @param callback The callback
     * @return This instance for chaining
     */
    @Override
    BaseResponsePromise<V> on(FutureCallback<V> callback);
}
