package com.atlassian.plugin.connect.plugin.module.context;

/**
 * Indicates that a resource that was expected to exist does not (possibly due to the user not having permission)
 */
public class ResourceNotFoundException extends Exception
{
    public ResourceNotFoundException(String message)
    {
        super(message);
    }
}
