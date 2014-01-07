package com.atlassian.plugin.connect.plugin.capabilities.schema;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.spring.scanner.ProductFilter;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.util.ProductFilterUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.report.ListProcessingReport;
import com.github.fge.jsonschema.report.ListReportProvider;
import com.github.fge.jsonschema.report.LogLevel;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

@ExportAsService(ConnectDescriptorValidator.class)
@Named
public class JsonDescriptorValidator implements ConnectDescriptorValidator
{
    private static final Logger log = LoggerFactory.getLogger(JsonDescriptorValidator.class);
    private static final JsonSchemaFactory factory = createSchemaFactory();

    private static JsonSchemaFactory createSchemaFactory()
    {
        return JsonSchemaFactory.newBuilder().setReportProvider(new ListReportProvider(LogLevel.ERROR,LogLevel.FATAL)).freeze();
    }

    private final ConnectSchemaLocator schemaLocator;

    @Inject
    public JsonDescriptorValidator(ConnectSchemaLocator schemaLocator)
    {
        this.schemaLocator = schemaLocator;
    }

    @Override
    public DescriptorValidationResult validate(String descriptor)
    {
        return validate(descriptor,ProductFilterUtil.getFilterForCurrentProduct());
    }

    @Override
    public boolean isConnectJson(String descriptor)
    {
        Gson gson = new Gson();
        boolean valid = false;
        
        try
        {
            JsonElement root = gson.fromJson(descriptor,JsonElement.class);
            
            if(root.isJsonObject())
            {
                JsonObject jobj = root.getAsJsonObject();
                valid = (jobj.has(ConnectAddonBean.KEY_ATTR) && jobj.has(ConnectAddonBean.BASE_URL_ATTR));
            }
        }
        catch (Exception e)
        {
            valid = false;
        }
        
        return valid;
    }

    @Override
    public DescriptorValidationResult validate(String descriptor, ProductFilter productFilter)
    {
        DescriptorValidationResult result = null;
        try
        {
            JsonNode schemaNode = JsonLoader.fromString(schemaLocator.getSchema(productFilter));
            JsonSchema schema = factory.getJsonSchema(schemaNode);
            JsonNode descriptorNode = JsonLoader.fromString(descriptor);
            ListProcessingReport report = (ListProcessingReport) schema.validate(descriptorNode);
            result = new DescriptorValidationResult(report.isSuccess(),report.asJson().toString(),report.toString());
        }
        catch (ProcessingException e)
        {
            result = new DescriptorValidationResult(false,e.getProcessingMessage().asJson().toString(),e.getProcessingMessage().toString());
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unable to load atlassian connect schema", e);
        }

        return result;
    }
}
