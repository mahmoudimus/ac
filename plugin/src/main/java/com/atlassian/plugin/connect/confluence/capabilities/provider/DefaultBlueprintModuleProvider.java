package com.atlassian.plugin.connect.confluence.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.confluence.capabilities.descriptor.BlueprintContentTemplateModuleDescriptorFactory;
import com.atlassian.plugin.connect.confluence.capabilities.descriptor.BlueprintModuleDescriptorFactory;
import com.atlassian.plugin.connect.confluence.capabilities.descriptor.BlueprintWebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@ConfluenceComponent
@ExportAsDevService
public class DefaultBlueprintModuleProvider implements BlueprintModuleProvider
{
    public static final String DESCRIPTOR_KEY = "blueprints";
    public static final Class BEAN_CLASS = BlueprintModuleBean.class;
    
    private final BlueprintWebItemModuleDescriptorFactory blueprintModuleWebItemDescriptorFactory;
    private final BlueprintModuleDescriptorFactory blueprintModuleDescriptorFactory;
    private final BlueprintContentTemplateModuleDescriptorFactory blueprintContentTemplateModuleDescriptorFactory;

    @Autowired
    public DefaultBlueprintModuleProvider(BlueprintWebItemModuleDescriptorFactory blueprintModuleWebItemDescriptorFactory,
                                          BlueprintModuleDescriptorFactory blueprintModuleDescriptorFactory,
                                          BlueprintContentTemplateModuleDescriptorFactory blueprintContentTemplateModuleDescriptorFactory)
    {
        this.blueprintModuleWebItemDescriptorFactory = blueprintModuleWebItemDescriptorFactory;
        this.blueprintModuleDescriptorFactory = blueprintModuleDescriptorFactory;
        this.blueprintContentTemplateModuleDescriptorFactory = blueprintContentTemplateModuleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin theConnectPlugin, List<JsonObject> modules)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (JsonObject module : modules)
        {
            BlueprintModuleBean bean = new Gson().fromJson(module, BlueprintModuleBean.class);
            builder.add(
                    blueprintModuleWebItemDescriptorFactory.createModuleDescriptor(moduleProviderContext,
                            theConnectPlugin,
                            bean));
            builder.add(
                    blueprintContentTemplateModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext,
                            theConnectPlugin,
                            bean));
            builder.add(
                    blueprintModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext,
                            theConnectPlugin,
                            bean));

        }

        return builder.build();
    }

    @Override
    public Class getBeanClass()
    {
        return BEAN_CLASS;
    }

    @Override
    public String getDescriptorKey()
    {
        return DESCRIPTOR_KEY;
    }
}
