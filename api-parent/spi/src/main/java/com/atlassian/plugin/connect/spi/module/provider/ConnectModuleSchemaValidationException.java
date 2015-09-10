package com.atlassian.plugin.connect.spi.module.provider;

public class ConnectModuleSchemaValidationException extends ConnectModuleValidationException
{
    public ConnectModuleSchemaValidationException(String moduleType, String reportResult)
    {
        super(moduleType, "Modules failed to validate against the schema. " + reportResult);
    }
}
