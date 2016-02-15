package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidationException;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@ExportAsDevService
@Component
public class GsonConnectAddonBeanFactory implements ConnectAddonBeanFactory
{

    private final ConnectJsonSchemaValidator descriptorSchemaValidator;
    private final AddonBeanValidatorService addonBeanValidatorService;
    private final PluginRetrievalService pluginRetrievalService;
    private final PluginAccessor pluginAccessor;
    private Map<String, ConnectAddonBean> descriptorCache = Maps.newConcurrentMap();

    @Autowired
    public GsonConnectAddonBeanFactory(ConnectJsonSchemaValidator descriptorSchemaValidator,
            AddonBeanValidatorService addonBeanValidatorService,
            PluginRetrievalService pluginRetrievalService,
            PluginAccessor pluginAccessor)
    {
        this.descriptorSchemaValidator = descriptorSchemaValidator;
        this.addonBeanValidatorService = addonBeanValidatorService;
        this.pluginRetrievalService = pluginRetrievalService;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public ConnectAddonBean fromJson(final String jsonDescriptor) throws InvalidDescriptorException
    {
        return descriptorCache.computeIfAbsent(jsonDescriptor, GsonConnectAddonBeanFactory.this::fromJsonImpl);
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

    protected ConnectAddonBean fromJsonImpl(final String jsonDescriptor) throws InvalidDescriptorException
    {
        validateDescriptorAgainstShallowSchema(jsonDescriptor);
        ConnectAddonBean addon = deserializeDescriptor(jsonDescriptor);
        addonBeanValidatorService.validate(addon);
        return addon;
    }

    private void validateDescriptorAgainstShallowSchema(String jsonDescriptor)
    {
        try
        {
            descriptorSchemaValidator.assertValidDescriptor(jsonDescriptor, getShallowSchemaUrl());
        } catch (ConnectJsonSchemaValidationException e)
        {
            throw new InvalidDescriptorException(e.getMessage(), e.getI18nKey(), e.getI18nParameters());
        }
    }

    private ConnectAddonBean deserializeDescriptor(final String jsonDescriptor)
    {
        JsonElement element = new JsonParser().parse(jsonDescriptor);
        ShallowConnectAddonBean shallowBean = ConnectModulesGsonFactory.shallowAddonFromJson(element);
        ModuleListDeserializer moduleDeserializer = new PluggableModuleListDeserializer(pluginAccessor, shallowBean);

        Map<String, Supplier<List<ModuleBean>>> moduleList;
        moduleList = ConnectModulesGsonFactory.moduleListFromJson(element, moduleDeserializer);

        return new ConnectAddonBeanBuilder(shallowBean).withModuleList(moduleList).build();
    }

    private URL getShallowSchemaUrl()
    {
        return pluginRetrievalService.getPlugin().getResource("/schema/shallow-schema.json");
    }
}
