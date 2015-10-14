package com.atlassian.plugin.connect.spi.module;

/**
 * An exception thrown when JSON schema validation of a descriptor module fails.
 */
public class ConnectModuleSchemaValidationException extends ConnectModuleValidationException
{
    public ConnectModuleSchemaValidationException(String moduleType, String reportResult, String json)
    {
        super(moduleType, "Modules failed to validate against the schema. " + reportResult + "\n provided JSON was " + json);
    }
}
