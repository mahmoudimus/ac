package com.atlassian.plugin.connect.modules.schema;

public interface ConnectDescriptorValidator
{
    boolean isConnectJson(String descriptor);

    DescriptorValidationResult validate(String descriptor, String schema);
}
