package com.atlassian.plugin.remotable.api.service.http.bigpipe;

import com.atlassian.util.concurrent.Promise;

/**
 * A handle to the BigPipe HTML channel.
 *
 * @since 0.7
 */
public interface HtmlChannel extends Channel
{
    /**
     * Registers an HTML content promise on the HTML channel {@link BigPipe#HTML_CHANNEL_ID}.
     *
     * @param stringPromise A promise for the HTML content string; must not be null
     * @return A String that can be used to as injectable HTML that serves either as
     *         a placeholder for future content injection, or the content itself if
     *         the promise has already been resolved; never null
     * @throws NullPointerException If the stringPromise is null
     */
    String promiseContent(Promise<String> stringPromise)
        throws NullPointerException;
}
