package com.atlassian.plugin.connect.spi.descriptor;

import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidationException;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidationResult;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;

/**
 * An exception thrown when JSON schema validation of a descriptor module fails.
 */
public class ConnectModuleSchemaValidationException extends ConnectModuleValidationException
{

    private final ConnectJsonSchemaValidationResult validationResult;

    public ConnectModuleSchemaValidationException(ConnectModuleMeta moduleMeta, ConnectJsonSchemaValidationException e)
    {
        super(moduleMeta, e.getMessage(), e.getI18nKey(), e.getI18nParameters());
        validationResult = e.getValidationResult();
        initCause(e);
    }

    public ConnectJsonSchemaValidationResult getValidationResult()
    {
        return validationResult;
    }
}
