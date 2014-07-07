package com.atlassian.plugin.connect.testsupport.filter;

/**
 * A simple representation of an http response that can be used for pre-canned responses
 */
public class PrecannedResponse
{
    private final String requiredPath;
    private final int statusCode;

    public PrecannedResponse(String requiredPath, int statusCode)
    {

        this.requiredPath = requiredPath;
        this.statusCode = statusCode;
    }

    public String getRequiredPath()
    {
        return requiredPath;
    }

    public int getStatusCode()
    {
        return statusCode;
    }
}
