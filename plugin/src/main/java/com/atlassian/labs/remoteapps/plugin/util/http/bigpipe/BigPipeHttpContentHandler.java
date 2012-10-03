package com.atlassian.labs.remoteapps.plugin.util.http.bigpipe;

import com.atlassian.labs.remoteapps.plugin.util.http.HttpContentHandler;

/**
 * An accessor for content that is being rendered by big pipe
 */
public interface BigPipeHttpContentHandler extends HttpContentHandler
{
    /**
     * Gets the content to display to the page immediately.  If the content can be retrieved from
     * the cache, this will be the final content.  If not, a marker span will be returned to be
     * processed later by Javascript fed from an xhr long-poll call.
     */
    String getInitialContent();

    /**
     * This call does not block and should only be called once the content has been returned as
     * successfully retrieved.
     *
     * @return The final content, first processing it with a processor
     */
    String getFinalContent();

    /**
     * @return The unique id of the content
     */
    String getContentId();

    /**
     * Mark the content as complete, usually in the case of an error detected and handled in a different way
     */
    void markCompleted();
}
