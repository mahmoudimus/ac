package com.atlassian.plugin.connect.modules.schema;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import com.github.fge.jsonschema.core.report.ListReportProvider;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

public class JsonDescriptorValidator implements ConnectDescriptorValidator
{
    private static final JsonSchemaFactory factory = createSchemaFactory();

    private static JsonSchemaFactory createSchemaFactory()
    {
        return JsonSchemaFactory.newBuilder().setReportProvider(new ListReportProvider(LogLevel.ERROR, LogLevel.FATAL)).freeze();
    }

    @Override
    public boolean isConnectJson(String descriptor, boolean allowMalformedJson)
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
            // Don't fail just yet, maybe it *is* a Connect descriptor and just malformed.
            // We can report this case only if we actually get to the install handler.
            // This is a workaround for https://ecosystem.atlassian.net/browse/UPM-4356
            // TODO: remove once UPM-4356 is resolved

            valid = allowMalformedJson ? isMalformedConnectJson(descriptor) : false;
        }

        return valid;
    }

    private boolean isMalformedConnectJson(String descriptor)
    {
        String trimmedJson = descriptor.trim();
        return trimmedJson.startsWith("{")
                && trimmedJson.endsWith("}")
                && containsJsonProperty(trimmedJson, ConnectAddonBean.KEY_ATTR)
                && containsJsonProperty(trimmedJson, ConnectAddonBean.BASE_URL_ATTR);
    }

    private boolean containsJsonProperty(String descriptor, String property)
    {
        return descriptor.contains("\"" + property + "\"");
    }

    @Override
    public DescriptorValidationResult validate(String descriptor, String schema)
    {
        DescriptorValidationResult result;
        try
        {
            JsonNode descriptorNode = JsonLoader.fromString(descriptor);
            try
            {
                JsonNode schemaNode = JsonLoader.fromString(schema);
                JsonSchema jsonSchema = factory.getJsonSchema(schemaNode);
                ListProcessingReport report = (ListProcessingReport) jsonSchema.validate(descriptorNode);
                result = new DescriptorValidationResult(true, report.isSuccess(), report.asJson().toString(), report.toString());
            }
            catch (ProcessingException e)
            {
                result = new DescriptorValidationResult(true, false, e.getProcessingMessage().asJson().toString(), e.getProcessingMessage().toString());
            }
            catch (IOException e)
            {
                throw new IllegalStateException("Unable to load atlassian connect schema", e);
            }
        }
        catch (IOException e)
        {
            result = new DescriptorValidationResult(false, false, "{\"error\":\"JSON not well-formed\"}", e.getMessage());
        }
        return result;
    }
}
