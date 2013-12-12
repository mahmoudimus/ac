package com.atlassian.plugin.connect.plugin.capabilities.schema;

import java.io.IOException;

import com.atlassian.plugin.spring.scanner.ProductFilter;

public interface ConnectDescriptorValidator
{
    DescriptorValidationResult validate(String descriptor);
    DescriptorValidationResult validate(String descriptor,ProductFilter productFilter);
}
