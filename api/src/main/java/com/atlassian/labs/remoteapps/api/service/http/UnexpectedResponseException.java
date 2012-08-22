package com.atlassian.labs.remoteapps.api.service.http;

/**
 *
 */
public class UnexpectedResponseException extends RuntimeException
{
    private Response response;

    public UnexpectedResponseException(Response response)
    {
        this.response = response;
    }

    public Response getResponse()
    {
        return response;
    }
}
