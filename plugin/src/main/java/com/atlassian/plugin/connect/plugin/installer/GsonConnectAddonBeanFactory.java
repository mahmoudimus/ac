package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.service.IsDevModeService;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.modules.schema.DescriptorValidationResult;
import com.atlassian.plugin.connect.modules.schema.JsonDescriptorValidator;
import com.atlassian.plugin.connect.plugin.capabilities.schema.ConnectSchemaLocator;
import com.atlassian.plugin.connect.plugin.capabilities.validate.AddOnBeanValidatorService;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.github.fge.msgsimple.provider.LoadingMessageSourceProvider;
import com.google.common.base.Supplier;
import com.google.gson.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Component
public class GsonConnectAddonBeanFactory implements ConnectAddonBeanFactory, DisposableBean, InitializingBean
{
    private static final Logger log = LoggerFactory.getLogger(GsonConnectAddonBeanFactory.class);

    private final JsonDescriptorValidator jsonDescriptorValidator;
    private final ConnectSchemaLocator connectSchemaLocator;
    private final ApplicationProperties applicationProperties;
    private final I18nResolver i18nResolver;
    private final AddOnBeanValidatorService addOnBeanValidatorService;
    private final IsDevModeService isDevModeService;
    private final PluginAccessor pluginAccessor;

    @Autowired
    public GsonConnectAddonBeanFactory(final JsonDescriptorValidator jsonDescriptorValidator,
            final AddOnBeanValidatorService addOnBeanValidatorService, final ConnectSchemaLocator connectSchemaLocator,
            final ApplicationProperties applicationProperties, I18nResolver i18nResolver,
            IsDevModeService isDevModeService,
            PluginAccessor pluginAccessor)
    {
        this.jsonDescriptorValidator = jsonDescriptorValidator;
        this.addOnBeanValidatorService = addOnBeanValidatorService;
        this.connectSchemaLocator = connectSchemaLocator;
        this.applicationProperties = applicationProperties;
        this.i18nResolver = i18nResolver;
        this.isDevModeService = isDevModeService;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public ConnectAddonBean fromJson(final String jsonDescriptor) throws InvalidDescriptorException
    {
        return fromJson(jsonDescriptor,null);
    }

    @Override
    public ConnectAddonBean fromJson(String jsonDescriptor, Map<String, String> i18nCollector) throws InvalidDescriptorException
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
            throw new InvalidDescriptorException("Malformed connect descriptor: " + result.getReportAsString(), "connect.invalid.descriptor.malformed.json");
        }
        if (!result.isValid())
        {
            String exceptionMessage = "Invalid connect descriptor: " + result.getReportAsString();
            log.error(exceptionMessage);

            String i18nKey;
            Serializable[] params;
            if (isDevModeService.isDevMode())
            {
                i18nKey = "connect.install.error.remote.descriptor.validation.dev";
                String validationMessage = buildErrorMessage(result);
                params = new Serializable[] {validationMessage};
            }
            else
            {
                i18nKey = "connect.install.error.remote.descriptor.validation";
                params = new Serializable[] {applicationProperties.getDisplayName()};
            }
            throw new InvalidDescriptorException(exceptionMessage, i18nKey, params);
        }

        ConnectAddonBean addOn = fromJsonSkipValidation(jsonDescriptor,i18nCollector);
        addOnBeanValidatorService.validate(addOn);
        validateModules(addOn);
        
        return addOn;
    }

    @Override
    public ConnectAddonBean fromJsonSkipValidation(final String jsonDescriptor)
    {
        return fromJsonSkipValidation(jsonDescriptor,null);
    }

    @Override
    public ConnectAddonBean fromJsonSkipValidation(String jsonDescriptor, Map<String, String> i18nCollector)
    {
        try
        {
            JsonDeserializer moduleDeserializer = new PluginAwareModuleBeanDeserializer(pluginAccessor);
            return ConnectModulesGsonFactory.addonFromJsonWithI18nCollector(jsonDescriptor, i18nCollector, moduleDeserializer);
        }
        catch (Exception e)
        {
            String exceptionMessage = "Invalid connect descriptor: " + e.getMessage();
            log.error(exceptionMessage);
            throw new InvalidDescriptorException(exceptionMessage, "connect.install.error.remote.descriptor.validation", applicationProperties.getDisplayName());
        }
    }

    @Override
    public void destroy() throws Exception
    {
        //JDEV-29184 -  we need to explicitly clean up threads in the underlying msg-simple library provided by the json-schema-validator
        LoadingMessageSourceProvider.shutdown();
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        LoadingMessageSourceProvider.restartIfNeeded();
    }

    private String buildErrorMessage(DescriptorValidationResult result)
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
    
    private void validateModules(ConnectAddonBean addOn)
    {
        for (Map.Entry<String, Supplier<List<ModuleBean>>> entry : addOn.getModules().entrySet())
        {
            // This will validate the beans and cache the result
            entry.getValue().get();
        }
    }
}
