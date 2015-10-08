package com.atlassian.plugin.connect.api.descriptor;

public interface ConnectJsonSchemaValidationResult
{
    boolean isWellformed();

    boolean isValid();

    String getJsonReport();

    String getReportAsString();

    Iterable<String> getReportMessages();
}
