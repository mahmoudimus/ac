package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.modules.schema.DescriptorValidationResult;
import com.atlassian.plugin.connect.modules.schema.JsonDescriptorValidator;
import com.atlassian.plugin.connect.plugin.capabilities.schema.ConnectSchemaLocator;
import com.atlassian.plugin.connect.plugin.capabilities.validate.AddOnBeanValidatorService;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.sal.api.ApplicationProperties;
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
    private final AddOnBeanValidatorService addOnBeanValidatorService;

    @Autowired
    public GsonConnectAddonBeanFactory(final JsonDescriptorValidator jsonDescriptorValidator,
            final AddOnBeanValidatorService addOnBeanValidatorService, final ConnectSchemaLocator connectSchemaLocator,
            final ApplicationProperties applicationProperties)
    {
        this.jsonDescriptorValidator = jsonDescriptorValidator;
        this.addOnBeanValidatorService = addOnBeanValidatorService;
        this.connectSchemaLocator = connectSchemaLocator;
        this.applicationProperties = applicationProperties;
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
        if (!result.isSuccess())
        {
            String msg = "Invalid connect descriptor: " + result.getMessageReport();
            log.error(msg);
            throw new InvalidDescriptorException(msg, "connect.install.error.remote.descriptor.validation." +
                    applicationProperties.getDisplayName().toLowerCase());
        }

        ConnectAddonBean addOn = fromJsonSkipValidation(jsonDescriptor);
        addOnBeanValidatorService.validate(addOn);

        return addOn;
    }

    @Override
    public ConnectAddonBean fromJsonSkipValidation(final String jsonDescriptor)
    {
        return ConnectModulesGsonFactory.getGson().fromJson(jsonDescriptor, ConnectAddonBean.class);
    }
}
