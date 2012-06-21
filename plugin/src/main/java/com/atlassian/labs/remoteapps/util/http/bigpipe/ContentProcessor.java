package com.atlassian.labs.remoteapps.util.http.bigpipe;

/**
 * A callback to process the HTTP content retrieved
 */
public interface ContentProcessor
{
    String process(String value);
}
