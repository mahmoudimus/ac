package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.modules.schema.DescriptorValidationResult;
import com.atlassian.plugin.connect.modules.schema.JsonDescriptorValidator;
import com.atlassian.plugin.connect.plugin.capabilities.schema.ConnectSchemaLocator;
import com.atlassian.plugin.connect.plugin.capabilities.validate.AddOnBeanValidatorService;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *
 */
@Component
public class GsonConnectAddonBeanFactory implements ConnectAddonBeanFactory
{
    private static final Logger log = LoggerFactory.getLogger(GsonConnectAddonBeanFactory.class);

    private final JsonDescriptorValidator jsonDescriptorValidator;
    private final ConnectSchemaLocator connectSchemaLocator;
    private final ApplicationProperties applicationProperties;
    private final I18nResolver i18nResolver;
    private final AddOnBeanValidatorService addOnBeanValidatorService;

    @Autowired
    public GsonConnectAddonBeanFactory(final JsonDescriptorValidator jsonDescriptorValidator,
            final AddOnBeanValidatorService addOnBeanValidatorService, final ConnectSchemaLocator connectSchemaLocator,
            final ApplicationProperties applicationProperties, I18nResolver i18nResolver)
    {
        this.jsonDescriptorValidator = jsonDescriptorValidator;
        this.addOnBeanValidatorService = addOnBeanValidatorService;
        this.connectSchemaLocator = connectSchemaLocator;
        this.applicationProperties = applicationProperties;
        this.i18nResolver = i18nResolver;
    }

    @Override
    public ConnectAddonBean fromJson(final String jsonDescriptor) throws InvalidDescriptorException
    {
        final String schema;
        try
        {
            schema = connectSchemaLocator.getSchemaForCurrentProduct();
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to read JSON schema for descriptor", e);
        }

        DescriptorValidationResult result = jsonDescriptorValidator.validate(jsonDescriptor, schema);
        if (!result.isWellformed())
        {
            throw new InvalidDescriptorException("Malformed connect descriptor: " + result.getMessageReport(), "connect.invalid.descriptor.malformed.json");
        }
        if (!result.isValid())
        {
            String exceptionMessage = "Invalid connect descriptor: " + result.getMessageReport();
            log.error(exceptionMessage);
            String i18nMessage = i18nResolver.getText("connect.install.error.remote.descriptor.validation", applicationProperties.getDisplayName());
            throw new InvalidDescriptorException(exceptionMessage, i18nMessage);
        }

        ConnectAddonBean addOn = fromJsonSkipValidation(jsonDescriptor);
        addOnBeanValidatorService.validate(addOn);

        return addOn;
    }

    @Override
    public ConnectAddonBean fromJsonSkipValidation(final String jsonDescriptor) throws InvalidDescriptorException
    {
        try
        {
            return ConnectModulesGsonFactory.getGson().fromJson(jsonDescriptor, ConnectAddonBean.class);
        }
        catch (Exception e)
        {
            String exceptionMessage = "Invalid connect descriptor: " + e.getMessage();
            log.error(exceptionMessage);
            String i18nMessage = i18nResolver.getText("connect.install.error.remote.descriptor.validation", applicationProperties.getDisplayName());
            throw new InvalidDescriptorException(exceptionMessage, i18nMessage);
        }
    }
}
