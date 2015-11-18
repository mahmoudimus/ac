package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.connect.spi.descriptor.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ModuleMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * A base class for exception handlers for use with {@link ModuleMultimap#getValidModuleLists(Consumer)} that logs
 * to the system log.
 */
public class LoggingModuleValidationExceptionHandler implements Consumer<Exception>
{

    private static final Logger log = LoggerFactory.getLogger(ModuleMultimap.class);

    @Override
    public void accept(Exception e)
    {
        log.warn("An error occurred when deserializing modules", e);
        if (e instanceof ConnectModuleValidationRuntimeException)
        {
            ConnectModuleValidationException cause = ((ConnectModuleValidationRuntimeException) e).getCause();
            handleModuleValidationCause(cause);
        }
    }

    protected void handleModuleValidationCause(ConnectModuleValidationException cause)
    {}
}
