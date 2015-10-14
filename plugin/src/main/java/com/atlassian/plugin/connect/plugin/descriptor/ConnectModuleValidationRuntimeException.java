package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.connect.spi.module.ConnectModuleValidationException;

/**
 * A run-time exception wrapper around {@link ConnectModuleValidationException} to enable throwing out of Gson
 * deserialization.
 */
public class ConnectModuleValidationRuntimeException extends RuntimeException
{

    private final ConnectModuleValidationException cause;

    public ConnectModuleValidationRuntimeException(ConnectModuleValidationException cause)
    {
        super(cause);
        this.cause = cause;
    }

    @Override
    public synchronized ConnectModuleValidationException getCause()
    {
        return cause;
    }
}
