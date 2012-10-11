package com.atlassian.plugin.remotable.api.service.http;

import java.util.List;

/**
 * Thrown to indicate that a set of responses completed normally but that
 * one or more of them produced an unexpected status code.
 */
public class UnexpectedResponsesException extends RuntimeException
{
    private List<Response> responses;

    /**
     * Creates a new exception for the given responses.
     *
     * @param responses The list of responses
     */
    public UnexpectedResponsesException(List<Response> responses)
    {
        this.responses = responses;
    }

    /**
     * Returns the unexpected responses.
     *
     * @return The responses
     */
    public List<Response> getResponses()
    {
        return responses;
    }
}
