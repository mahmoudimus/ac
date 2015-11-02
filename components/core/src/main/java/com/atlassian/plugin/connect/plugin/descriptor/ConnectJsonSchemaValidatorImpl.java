package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidationException;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidationResult;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.plugin.util.IsDevModeService;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.sal.api.ApplicationProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import com.github.fge.jsonschema.core.report.ListReportProvider;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.msgsimple.provider.LoadingMessageSourceProvider;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;

@Component
@ExportAsService(ConnectJsonSchemaValidator.class)
public class ConnectJsonSchemaValidatorImpl implements ConnectJsonSchemaValidator, InitializingBean, DisposableBean
{

    private final JsonSchemaFactory factory;
    private final ApplicationProperties applicationProperties;
    private final IsDevModeService isDevModeService;

    @Autowired
    public ConnectJsonSchemaValidatorImpl(ApplicationProperties applicationProperties, IsDevModeService isDevModeService)
    {
        this.factory = JsonSchemaFactory.newBuilder()
                .setReportProvider(new ListReportProvider(LogLevel.ERROR, LogLevel.FATAL))
                .freeze();
        this.applicationProperties = applicationProperties;
        this.isDevModeService = isDevModeService;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        LoadingMessageSourceProvider.restartIfNeeded();
    }

    @Override
    public void destroy() throws Exception
    {
        //JDEV-29184 -  we need to explicitly clean up threads in the underlying msg-simple library provided by the json-schema-validator
        LoadingMessageSourceProvider.shutdown();
    }

    @Override
    public ConnectJsonSchemaValidationResult validateDescriptor(String descriptor, URL schemaUrl)
    {
        JsonSchema jsonSchema = loadSchema(schemaUrl);

        ConnectJsonSchemaValidationResult result;
        try
        {
            JsonNode descriptorNode = JsonLoader.fromReader(new JsonDescriptorStringReader(descriptor));
            result = validate(descriptorNode, jsonSchema);
        }
        catch (IOException e)
        {
            result = new DescriptorValidationResult(false, false, "{\"error\":\"JSON not well-formed\"}", e.getMessage());
        }

        return result;
    }

    @Override
    public void assertValidDescriptor(String descriptor, URL schemaUrl) throws ConnectJsonSchemaValidationException
    {
        ConnectJsonSchemaValidationResult result = validateDescriptor(descriptor, schemaUrl);
        if (!result.isWellformed())
        {
            throw new InvalidDescriptorException("Malformed connect descriptor: " + result.getReportAsString(), "connect.invalid.descriptor.malformed.json", result.getReportAsString());
        }
        if (!result.isValid())
        {
            String exceptionMessage = "Invalid connect descriptor: " + result.getReportAsString();

            String i18nKey;
            Serializable[] params;
            if (isDevModeService.isDevMode())
            {
                i18nKey = "connect.install.error.remote.descriptor.validation.dev";
                String validationMessage = buildHtmlErrorMessage(result);
                params = new Serializable[] {validationMessage};
            }
            else
            {
                i18nKey = "connect.install.error.remote.descriptor.validation";
                params = new Serializable[] {applicationProperties.getDisplayName()};
            }
            throw new InvalidDescriptorException(exceptionMessage, i18nKey, params);
        }
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

    private String buildHtmlErrorMessage(ConnectJsonSchemaValidationResult result)
    {
        StringBuilder messageBuilder = new StringBuilder("<ul>");
        for (String message : result.getReportMessages())
        {
            messageBuilder.append("<li>");
            messageBuilder.append(message);
        }
        messageBuilder.append("</ul>");
        return messageBuilder.toString();
    }

    /**
     * A string reader with a custom string representation, for sensible Jackson error messages.
     */
    private static class JsonDescriptorStringReader extends StringReader
    {

        /**
         * Creates a new string reader.
         *
         * @param s String providing the character stream.
         */
        public JsonDescriptorStringReader(String s)
        {
            super(s);
        }

        /**
         * Returns an empty string (to avoid the {@link Object#toString()} behavior inherited by {@link StringReader}.
         *
         * @return an empty string
         */
        @Override
        public String toString()
        {
            return "";
        }
    }
}
