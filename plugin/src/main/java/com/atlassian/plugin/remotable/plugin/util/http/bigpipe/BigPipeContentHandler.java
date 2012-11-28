package com.atlassian.plugin.remotable.plugin.util.http.bigpipe;

import com.atlassian.util.concurrent.Promise;

/**
 * An accessor for content that is being rendered by big pipe
 */
public interface BigPipeContentHandler
{
    /*
     * @return The unique id of the content
     */
    String getContentId();

    Promise<String> getContent();

    String getCurrentContent();
}
