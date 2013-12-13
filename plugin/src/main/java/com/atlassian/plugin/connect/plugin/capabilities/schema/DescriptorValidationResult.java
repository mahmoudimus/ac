package com.atlassian.plugin.connect.plugin.capabilities.schema;

public class DescriptorValidationResult
{
    private final boolean success;
    private final String jsonReport;
    private final String messageReport;

    public DescriptorValidationResult(boolean success, String jsonReport, String messageReport)
    {
        this.success = success;
        this.jsonReport = jsonReport;
        this.messageReport = messageReport;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public String getJsonReport()
    {
        return jsonReport;
    }

    public String getMessageReport()
    {
        return messageReport;
    }
}
