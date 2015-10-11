package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidationResult;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.service.IsDevModeService;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.capabilities.validate.AddOnBeanValidatorService;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@ExportAsDevService
@Component
public class GsonConnectAddonBeanFactory implements ConnectAddonBeanFactory
{
    private static final Logger log = LoggerFactory.getLogger(GsonConnectAddonBeanFactory.class);

    private final ConnectJsonSchemaValidator descriptorSchemaValidator;
    private final ApplicationProperties applicationProperties;
    private final AddOnBeanValidatorService addOnBeanValidatorService;
    private final PluginRetrievalService pluginRetrievalService;
    private final PluginAccessor pluginAccessor;
    private final IsDevModeService isDevModeService;
    private Map<String, ConnectAddonBean> descriptorCache = Maps.newConcurrentMap();

    @Autowired
    public GsonConnectAddonBeanFactory(ConnectJsonSchemaValidator descriptorSchemaValidator,
            AddOnBeanValidatorService addOnBeanValidatorService,
            ApplicationProperties applicationProperties,
            PluginRetrievalService pluginRetrievalService,
            PluginAccessor pluginAccessor,
            IsDevModeService isDevModeService)
    {
        this.descriptorSchemaValidator = descriptorSchemaValidator;
        this.addOnBeanValidatorService = addOnBeanValidatorService;
        this.pluginRetrievalService = pluginRetrievalService;
        this.applicationProperties = applicationProperties;
        this.isDevModeService = isDevModeService;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public ConnectAddonBean fromJson(final String jsonDescriptor) throws InvalidDescriptorException
    {
        return descriptorCache.computeIfAbsent(jsonDescriptor, new Function<String, ConnectAddonBean>()
        {

            @Override
            public ConnectAddonBean apply(String descriptor)
            {
                return fromJsonImpl(descriptor);
            }
        });
    }

    @Override
    public void remove(String jsonDescriptor)
    {
        descriptorCache.remove(jsonDescriptor);
    }

    @Override
    public void removeAll()
    {
        descriptorCache.clear();
    }

    private ConnectAddonBean fromJsonImpl(final String jsonDescriptor) throws InvalidDescriptorException
    {
        validateDescriptorAgainstSchema(jsonDescriptor);
        ConnectAddonBean addon = deserializeDescriptor(jsonDescriptor);
        addOnBeanValidatorService.validate(addon);
        return addon;
    }

    private void validateDescriptorAgainstSchema(String jsonDescriptor)
    {
        ConnectJsonSchemaValidationResult result = descriptorSchemaValidator.validate(jsonDescriptor, getShallowSchemaUrl());
        assertValidDescriptorValidationResult(result);
    }

    private ConnectAddonBean deserializeDescriptor(final String jsonDescriptor)
    {
        try
        {
            JsonElement element = new JsonParser().parse(jsonDescriptor);
            ShallowConnectAddonBean shallowBean = ConnectModulesGsonFactory.shallowAddonFromJson(element);
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

    private URL getShallowSchemaUrl()
    {
        return pluginRetrievalService.getPlugin().getResource("/schema/shallow-schema.json");
    }

    private void assertValidDescriptorValidationResult(ConnectJsonSchemaValidationResult result)
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

    private String buildErrorMessage(ConnectJsonSchemaValidationResult result)
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
}
