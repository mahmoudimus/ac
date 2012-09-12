package com.atlassian.labs.remoteapps.api.service.http;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.api.PromiseCallback;
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
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> on(int statusCode, PromiseCallback<V> callback);

    // Informational (1xx) Selectors

    /**
     * Register a callback to respond to 'informational' (1xx) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> informational(PromiseCallback<V> callback);

    // Successful (2xx) Selectors

    /**
     * Register a callback to respond to 'successful' (2xx) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> successful(PromiseCallback<V> callback);

    /**
     * Register a callback to respond to 'ok' (200) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> ok(PromiseCallback<V> callback);

    /**
     * Register a callback to respond to 'created' (201) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> created(PromiseCallback<V> callback);

    /**
     * Register a callback to respond to 'no content' (204) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> noContent(PromiseCallback<V> callback);

    // Redirection (3xx) Selectors

    /**
     * Register a callback to respond to 'redirection' (3xx) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> redirection(PromiseCallback<V> callback);

    /**
     * Register a callback to respond to 'see other' (303) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> seeOther(PromiseCallback<V> callback);

    /**
     * Register a callback to respond to 'not modified' (304) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> notModified(PromiseCallback<V> callback);

    // Client Error (4xx) Selectors

    /**
     * Register a callback to respond to 'client error' (4xx) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> clientError(PromiseCallback<V> callback);

    /**
     * Register a callback to respond to 'bad request' (400) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> badRequest(PromiseCallback<V> callback);

    /**
     * Register a callback to respond to 'unauthorized' (401) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> unauthorized(PromiseCallback<V> callback);

    /**
     * Register a callback to respond to 'forbidden' (403) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> forbidden(PromiseCallback<V> callback);

    /**
     * Register a callback to respond to 'not found' (404) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> notFound(PromiseCallback<V> callback);

    /**
     * Register a callback to respond to 'conflict' (409) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> conflict(PromiseCallback<V> callback);

    // Server Error (5xx) Selectors

    /**
     * Register a callback to respond to 'server error' (5xx) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> serverError(PromiseCallback<V> callback);

    /**
     * Register a callback to respond to 'internal server error' (500) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> internalServerError(PromiseCallback<V> callback);

    /**
     * Register a callback to respond to 'service unavailable' (503) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> serviceUnavailable(PromiseCallback<V> callback);

    // Aggregate Selectors

    /**
     * Register a callback to respond to all error (4xx and 5xx) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> error(PromiseCallback<V> callback);

    /**
     * Register a callback to respond to all non-'successful' (1xx, 3xx, 4xx, 5xx) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> notSuccessful(PromiseCallback<V> callback);

    /**
     * Register a callback to respond to all other HTTP responses (i.e. those not explcitly registered for).
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    BaseResponsePromise<V> others(PromiseCallback<V> callback);

    /**
     * Register a callback to respond to all completed (1xx, 2xx, 3xx, 4xx, and 5xx) HTTP responses.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    @Override
    BaseResponsePromise<V> done(PromiseCallback<V> callback);

    // Exception Selectors

    /**
     * Register a callback to respond to exceptions thrown while executing the HTTP request.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    @Override
    BaseResponsePromise<V> fail(PromiseCallback<Throwable> callback);

    // Universal Selectors

    /**
     * Register a future callback to respond to all completed HTTP responses and exceptions thrown
     * while executing the HTTP request.
     *
     * @param callback The callback
     * @return This BaseResponsePromise<V> instance for chaining
     */
    @Override
    BaseResponsePromise<V> then(FutureCallback<V> callback);
}
