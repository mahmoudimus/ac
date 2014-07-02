package com.atlassian.plugin.connect.testsupport.filter;

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
