package com.atlassian.plugin.connect.modules.schema;

import java.io.IOException;

import javax.inject.Named;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.report.ListProcessingReport;
import com.github.fge.jsonschema.report.ListReportProvider;
import com.github.fge.jsonschema.report.LogLevel;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class JsonDescriptorValidator implements ConnectDescriptorValidator
{
    private static final Logger log = LoggerFactory.getLogger(JsonDescriptorValidator.class);
    private static final JsonSchemaFactory factory = createSchemaFactory();

    private static JsonSchemaFactory createSchemaFactory()
    {
        return JsonSchemaFactory.newBuilder().setReportProvider(new ListReportProvider(LogLevel.ERROR, LogLevel.FATAL)).freeze();
    }

    @Override
    public boolean isConnectJson(String descriptor)
    {
        Gson gson = new Gson();
        boolean valid = false;

        try
        {
            JsonElement root = gson.fromJson(descriptor, JsonElement.class);

            if (root.isJsonObject())
            {
                JsonObject jobj = root.getAsJsonObject();
                valid = (jobj.has(ConnectAddonBean.KEY_ATTR) && jobj.has(ConnectAddonBean.BASE_URL_ATTR));
            }
        }
        catch (JsonSyntaxException e)
        {
            log.trace(e.getMessage(), e);
            valid = false;
        }

        return valid;
    }

    @Override
    public DescriptorValidationResult validate(String descriptor, String schema)
    {
        DescriptorValidationResult result = null;
        try
        {
            JsonNode schemaNode = JsonLoader.fromString(schema);
            JsonSchema jsonSchema = factory.getJsonSchema(schemaNode);
            JsonNode descriptorNode = JsonLoader.fromString(descriptor);
            ListProcessingReport report = (ListProcessingReport) jsonSchema.validate(descriptorNode);
            result = new DescriptorValidationResult(report.isSuccess(), report.asJson().toString(), report.toString());
        }
        catch (ProcessingException e)
        {
            result = new DescriptorValidationResult(false, e.getProcessingMessage().asJson().toString(), e.getProcessingMessage().toString());
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unable to load atlassian connect schema", e);
        }

        return result;
    }
}
