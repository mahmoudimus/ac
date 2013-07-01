package com.atlassian.plugin.remotable.spi;

/**
 * If the installation failed
 */
public class InstallationFailedException extends RuntimeException
{
    public InstallationFailedException(String message)
    {
        super(message);
    }

    public InstallationFailedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InstallationFailedException(Throwable cause)
    {
        super(cause);
    }
}
