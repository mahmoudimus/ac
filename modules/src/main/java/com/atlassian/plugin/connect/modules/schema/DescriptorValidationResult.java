package com.atlassian.plugin.connect.modules.schema;

public class DescriptorValidationResult
{
    private final boolean isWellformed;
    private final boolean isValid;
    private final String jsonReport;
    private final String messageReport;

    public DescriptorValidationResult(boolean isWellFormed, boolean isValid, String jsonReport, String messageReport)
    {
        this.isWellformed = isWellFormed;
        this.isValid = isValid;
        this.jsonReport = jsonReport;
        this.messageReport = messageReport;
    }

    public boolean isWellformed()
    {
        return isWellformed;
    }

    public boolean isValid()
    {
        return isValid;
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
