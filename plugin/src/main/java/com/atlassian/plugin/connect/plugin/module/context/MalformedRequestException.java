package com.atlassian.plugin.connect.plugin.module.context;

/**
 * Indicates that the request was malformed in some way
 * TODO: This should not extend ResourceNotFoundException. Just a temp hack
 */
public class MalformedRequestException extends ResourceNotFoundException
{
    public MalformedRequestException(String message)
    {
        super(message);
    }
}
