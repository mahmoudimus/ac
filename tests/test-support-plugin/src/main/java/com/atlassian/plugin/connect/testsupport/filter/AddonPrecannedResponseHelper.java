package com.atlassian.plugin.connect.testsupport.filter;

import com.atlassian.fugue.Option;

/**
 * A helper that supports queuing pre-canned responses to Http requests. Test classes get the helper from some servlet
 * or filter that supports pre-canned responses and adds responses to the queue. The servlet will later pull the latest
 * pre-canned response using the poll method
 */
public interface AddonPrecannedResponseHelper
{
    /**
     * Queues a pre-canned response with the given status code. If the requiredPath parameter is not null then it
     * indicates that the response should only be returned if the request path matches
     */
    void queuePrecannedResponse(String requiredPath, int statusCode);

    /**
     * Takes the latest precanned response if it exists or None if nothing queued
     */
    Option<PrecannedResponse> poll();
}
