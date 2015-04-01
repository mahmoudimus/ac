package com.atlassian.plugin.connect.modules.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class DescriptorValidationResult
{
    private final boolean isWellformed;
    private final boolean isValid;
    private final String jsonReport;
    private final String messageReport;
    private ListProcessingReport report;

    public DescriptorValidationResult(boolean isWellFormed, boolean isValid, String jsonReport, String messageReport)
    {
        this.isWellformed = isWellFormed;
        this.isValid = isValid;
        this.jsonReport = jsonReport;
        this.messageReport = messageReport;
    }

    public DescriptorValidationResult(ListProcessingReport report)
    {
        this(true, report.isSuccess(), report.asJson().toString(), report.toString());
        this.report = report;
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

    public String getReportAsString()
    {
        return messageReport;
    }

    public Iterable<String> getReportMessages()
    {
        return Iterables.transform(report, new Function<ProcessingMessage, String>()
        {
            @Override
            public String apply(ProcessingMessage processingMessage)
            {
                JsonNode messageNode = processingMessage.asJson();
                if (messageNode.has("instance"))
                {
                    JsonNode instanceNode = messageNode.get("instance");
                    if (instanceNode.has("pointer"))
                    {
                        JsonNode pointerNode = instanceNode.get("pointer");
                        String pointer = pointerNode.asText();
                        return String.format("%s: %s", pointer, processingMessage.getMessage());
                    }
                }
                return processingMessage.getMessage();
            }
        });
    }
}
