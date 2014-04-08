package com.atlassian.plugin.connect.modules.schema;

public interface ConnectDescriptorValidator
{
    boolean isConnectJson(String descriptor, boolean allowMalformedJson);

    DescriptorValidationResult validate(String descriptor, String schema);
}
