package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.BlueprintModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.BlueprintWebItemModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean.newWebItemBean;

@Component
@ExportAsDevService
public class DefaultBlueprintModuleProvider implements BlueprintModuleProvider {

    private final BlueprintWebItemModuleDescriptorFactory blueprintModuleWebItemDescriptorFactory;
    private final BlueprintModuleDescriptorFactory blueprintModuleDescriptorFactory;

    @Autowired
    public DefaultBlueprintModuleProvider(BlueprintWebItemModuleDescriptorFactory blueprintModuleWebItemDescriptorFactory,
                                          BlueprintModuleDescriptorFactory blueprintModuleDescriptorFactory) {
        this.blueprintModuleWebItemDescriptorFactory = blueprintModuleWebItemDescriptorFactory;
        this.blueprintModuleDescriptorFactory = blueprintModuleDescriptorFactory;
    }


    @Override
    public List<ModuleDescriptor> provideModules(ConnectAddonBean connectAddonBean, Plugin theConnectPlugin,
                                                 String jsonFieldName, List<BlueprintModuleBean> beans) {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (BlueprintModuleBean bean : beans) {

            BlueprintModuleBean blueprintModuleBean = newWebItemBean()
                    .withName(bean.getName())
                    .withKey(bean.getRawKey())
                    .build();

            builder.add(blueprintModuleWebItemDescriptorFactory.createModuleDescriptor(connectAddonBean,
                    theConnectPlugin,
                    blueprintModuleBean));
            builder.add(blueprintModuleDescriptorFactory.createModuleDescriptor(connectAddonBean,
                    theConnectPlugin,
                    blueprintModuleBean));

        }

        return builder.build();
    }
}
