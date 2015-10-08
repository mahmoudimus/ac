package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidationResult;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import com.github.fge.jsonschema.core.report.ListReportProvider;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
@ExportAsService(ConnectJsonSchemaValidator.class)
public class ConnectJsonSchemaValidatorImpl implements ConnectJsonSchemaValidator
{

    private final JsonSchemaFactory factory;

    public ConnectJsonSchemaValidatorImpl()
    {
        this.factory = JsonSchemaFactory.newBuilder()
                .setReportProvider(new ListReportProvider(LogLevel.ERROR, LogLevel.FATAL))
                .freeze();
    }

    @Override
    public ConnectJsonSchemaValidationResult validate(String descriptor, URL schemaUrl)
    {
        JsonSchema jsonSchema = loadSchema(schemaUrl);

        ConnectJsonSchemaValidationResult result;
        try
        {
            JsonNode descriptorNode = JsonLoader.fromString(descriptor);
            result = validate(descriptorNode, jsonSchema);
        }
        catch (IOException e)
        {
            result = new DescriptorValidationResult(false, false, "{\"error\":\"JSON not well-formed\"}", e.getMessage());
        }

        return result;
    }

    private JsonSchema loadSchema(URL schemaUrl)
    {
        String schema;
        try
        {
            schema = IOUtils.toString(schemaUrl);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unable to read from schema URL", e);
        }

        try
        {
            JsonNode schemaNode = JsonLoader.fromString(schema);
            return factory.getJsonSchema(schemaNode);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unable to deserialize schema", e);
        }
        catch (ProcessingException e)
        {
            throw new IllegalStateException("Unable to build schema from JSON", e);
        }
    }

    private ConnectJsonSchemaValidationResult validate(JsonNode descriptorNode, JsonSchema schema)
    {
        ConnectJsonSchemaValidationResult result;
        try
        {
            ListProcessingReport report = (ListProcessingReport) schema.validate(descriptorNode);
            result = new DescriptorValidationResult(report);
        }
        catch (ProcessingException e)
        {
            result = new DescriptorValidationResult(true, false, e.getProcessingMessage().asJson().toString(), e.getProcessingMessage().toString());
        }
        return result;
    }
}
