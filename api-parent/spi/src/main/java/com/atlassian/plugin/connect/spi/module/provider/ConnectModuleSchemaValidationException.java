package com.atlassian.plugin.connect.spi.module.provider;

public class ConnectModuleSchemaValidationException extends ConnectModuleValidationException
{
    public ConnectModuleSchemaValidationException(String moduleType, String reportResult, String json)
    {
        super(moduleType, "Modules failed to validate against the schema. " + reportResult + "\n provided JSON was " + json);
    }
}
