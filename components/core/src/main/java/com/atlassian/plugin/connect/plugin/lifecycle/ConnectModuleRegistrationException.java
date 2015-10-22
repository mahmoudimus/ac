package com.atlassian.plugin.connect.plugin.lifecycle;

/**
 * An exception thrown when JSON schema validation fails.
 */
public class ConnectModuleRegistrationException extends Exception
{

    public ConnectModuleRegistrationException(String message)
    {
        super(message);
    }

    public ConnectModuleRegistrationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
