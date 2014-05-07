package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.BlueprintContentTemplateModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.BlueprintModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.BlueprintWebItemModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ExportAsDevService
public class DefaultBlueprintModuleProvider implements BlueprintModuleProvider {

    private final BlueprintWebItemModuleDescriptorFactory blueprintModuleWebItemDescriptorFactory;
    private final BlueprintModuleDescriptorFactory blueprintModuleDescriptorFactory;
    private final BlueprintContentTemplateModuleDescriptorFactory blueprintContentTemplateModuleDescriptorFactory;

    @Autowired
    public DefaultBlueprintModuleProvider(BlueprintWebItemModuleDescriptorFactory blueprintModuleWebItemDescriptorFactory,
                                          BlueprintModuleDescriptorFactory blueprintModuleDescriptorFactory,
                                          BlueprintContentTemplateModuleDescriptorFactory blueprintContentTemplateModuleDescriptorFactory) {
        this.blueprintModuleWebItemDescriptorFactory = blueprintModuleWebItemDescriptorFactory;
        this.blueprintModuleDescriptorFactory = blueprintModuleDescriptorFactory;
        this.blueprintContentTemplateModuleDescriptorFactory = blueprintContentTemplateModuleDescriptorFactory;
    }


    @Override
    public List<ModuleDescriptor> provideModules(ConnectAddonBean connectAddonBean, Plugin theConnectPlugin,
                                                 String jsonFieldName, List<BlueprintModuleBean> beans) {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (BlueprintModuleBean bean : beans) {

            builder.add(
                    blueprintModuleWebItemDescriptorFactory.createModuleDescriptor(connectAddonBean,
                    theConnectPlugin,
                    bean));
            builder.add(
                    blueprintContentTemplateModuleDescriptorFactory.createModuleDescriptor(connectAddonBean,
                    theConnectPlugin,
                    bean));
            builder.add(
                    blueprintModuleDescriptorFactory.createModuleDescriptor(connectAddonBean,
                    theConnectPlugin,
                    bean));

        }

        return builder.build();
    }
}
