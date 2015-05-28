package com.atlassian.plugin.connect.core.exception;

/**
 * This is thrown when we can't load a ModuleProvider class using Class.forName
 * It's a RuntimeException because we already have compile-time checks for this
 * and regardless, if this is thrown, there's nothing any code could possibly do
 * to recover from it.
 */
public class ModuleProviderNotFoundException extends RuntimeException
{
    public ModuleProviderNotFoundException()
    {
    }

    public ModuleProviderNotFoundException(String message)
    {
        super(message);
    }

    public ModuleProviderNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ModuleProviderNotFoundException(Throwable cause)
    {
        super(cause);
    }

}
