package com.atlassian.labs.remoteapps.api.service.http;

/**
 *
 */
public class UnexpectedResponseException extends RuntimeException
{
    private Response response;

    /**
     *
     *
     * @param response
     */
    public UnexpectedResponseException(Response response)
    {
        this.response = response;
    }

    /**
     *
     *
     * @return
     */
    public Response getResponse()
    {
        return response;
    }
}
