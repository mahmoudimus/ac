package com.atlassian.plugin.connect.confluence.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.confluence.capabilities.descriptor.BlueprintContentTemplateModuleDescriptorFactory;
import com.atlassian.plugin.connect.confluence.capabilities.descriptor.BlueprintModuleDescriptorFactory;
import com.atlassian.plugin.connect.confluence.capabilities.descriptor.BlueprintWebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.spi.module.AbstractConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@ConfluenceComponent
@ExportAsDevService
public class DefaultBlueprintModuleProvider extends AbstractConnectModuleProvider<BlueprintModuleBean> implements BlueprintModuleProvider
{

    private static final BlueprintModuleMeta META = new BlueprintModuleMeta();

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
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin theConnectPlugin, List<BlueprintModuleBean> beans)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (BlueprintModuleBean bean : beans)
        {
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
    public String getSchemaPrefix()
    {
        return "confluence";
    }

    @Override
    public ConnectModuleMeta<BlueprintModuleBean> getMeta()
    {
        return META;
    }
}
