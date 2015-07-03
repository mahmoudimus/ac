package com.atlassian.plugin.connect.crowd.usermanagement.api;

/**
 * A container for the many and varied exceptions that Crowd can throw, between
 * which we don't always wish to distinguish.
 */
public class ConnectCrowdException extends Exception
{
    public ConnectCrowdException()
    {
    }

    public ConnectCrowdException(Exception e)
    {
        super(e);
    }
}
