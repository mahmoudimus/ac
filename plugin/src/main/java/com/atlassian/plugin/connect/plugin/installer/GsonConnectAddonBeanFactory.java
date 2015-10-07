package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.service.IsDevModeService;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.modules.schema.DescriptorValidationResult;
import com.atlassian.plugin.connect.modules.schema.JsonDescriptorValidator;
import com.atlassian.plugin.connect.plugin.capabilities.schema.ConnectSchemaLocator;
import com.atlassian.plugin.connect.plugin.capabilities.validate.AddOnBeanValidatorService;
import com.atlassian.plugin.connect.plugin.capabilities.validate.impl.ModuleValidator;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.ApplicationProperties;
import com.github.fge.msgsimple.provider.LoadingMessageSourceProvider;
import com.google.common.base.Supplier;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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

@ExportAsDevService
@Component
public class GsonConnectAddonBeanFactory implements ConnectAddonBeanFactory, DisposableBean, InitializingBean
{
    private static final Logger log = LoggerFactory.getLogger(GsonConnectAddonBeanFactory.class);

    private final JsonDescriptorValidator jsonDescriptorValidator;
    private final ConnectSchemaLocator connectSchemaLocator;
    private final ApplicationProperties applicationProperties;
    private final AddOnBeanValidatorService addOnBeanValidatorService;
    private final IsDevModeService isDevModeService;
    private final PluginAccessor pluginAccessor;

    @Autowired
    public GsonConnectAddonBeanFactory(final JsonDescriptorValidator jsonDescriptorValidator,
            final AddOnBeanValidatorService addOnBeanValidatorService, final ConnectSchemaLocator connectSchemaLocator,
            final ApplicationProperties applicationProperties,
            IsDevModeService isDevModeService,
            PluginAccessor pluginAccessor)
    {
        this.jsonDescriptorValidator = jsonDescriptorValidator;
        this.addOnBeanValidatorService = addOnBeanValidatorService;
        this.connectSchemaLocator = connectSchemaLocator;
        this.applicationProperties = applicationProperties;
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
        validateDescriptorAgainstSchema(jsonDescriptor);

        ConnectAddonBean addon = fromJsonSkipValidation(jsonDescriptor,i18nCollector);
        validateModules(addon);
        addOnBeanValidatorService.validate(addon);

        return addon;
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
            JsonElement element = new JsonParser().parse(jsonDescriptor);
            ShallowConnectAddonBean shallowBean = ConnectModulesGsonFactory.shallowAddonFromJsonWithI18nCollector(element, i18nCollector);
            ModuleListDeserializer moduleDeserializer = new ModuleListDeserializer(new PluginAvailableModuleTypes(pluginAccessor, shallowBean));
            Map<String, Supplier<List<ModuleBean>>> moduleList = ConnectModulesGsonFactory.moduleListFromJson(element, moduleDeserializer);

            return new ConnectAddonBeanBuilder(shallowBean).withModuleList(moduleList).build();
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

    private void validateDescriptorAgainstSchema(String jsonDescriptor)
    {
        final String schema = getShallowSchema();
        DescriptorValidationResult result = jsonDescriptorValidator.validate(jsonDescriptor, schema);
        assertValidDescriptorValidationResult(result);
    }

    private String getShallowSchema()
    {
        final String schema;
        try
        {
            schema = connectSchemaLocator.getShallowSchema();
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to read JSON schema for descriptor", e);
        }
        return schema;
    }

    private void assertValidDescriptorValidationResult(DescriptorValidationResult result)
    {
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

    // TODO: I've implemented this in the ModuleValidator class, where I think it makes more sense,
    // but it should be called before PageConditionsValidator and WebHookScopeValidator because those guys try to get modules.
    // Can't work out how to guarantee the ordering of the validators.
    private void validateModules(ConnectAddonBean addon)
    {
        new ModuleValidator().validate(addon);
    }
}
