package com.atlassian.plugin.connect.confluence.blueprint;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.confluence.AbstractConfluenceConnectModuleProvider;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@ConfluenceComponent
@ExportAsDevService
public class BlueprintModuleProviderImpl extends AbstractConfluenceConnectModuleProvider<BlueprintModuleBean>
        implements BlueprintModuleProvider
{

    private static final BlueprintModuleMeta META = new BlueprintModuleMeta();

    private final BlueprintWebItemModuleDescriptorFactory blueprintModuleWebItemDescriptorFactory;
    private final BlueprintModuleDescriptorFactory blueprintModuleDescriptorFactory;
    private final BlueprintContentTemplateModuleDescriptorFactory blueprintContentTemplateModuleDescriptorFactory;

    @Autowired
    public BlueprintModuleProviderImpl(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            BlueprintWebItemModuleDescriptorFactory blueprintModuleWebItemDescriptorFactory,
            BlueprintModuleDescriptorFactory blueprintModuleDescriptorFactory,
            BlueprintContentTemplateModuleDescriptorFactory blueprintContentTemplateModuleDescriptorFactory)
    {
        super(pluginRetrievalService, schemaValidator);
        this.blueprintModuleWebItemDescriptorFactory = blueprintModuleWebItemDescriptorFactory;
        this.blueprintModuleDescriptorFactory = blueprintModuleDescriptorFactory;
        this.blueprintContentTemplateModuleDescriptorFactory = blueprintContentTemplateModuleDescriptorFactory;
    }

    @Override
    public ConnectModuleMeta<BlueprintModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<BlueprintModuleBean> modules, ConnectAddonBean addon)
    {
        Plugin plugin = pluginRetrievalService.getPlugin();
        List<ModuleDescriptor> descriptors = new ArrayList<>();
        for (BlueprintModuleBean blueprint : modules)
        {
            descriptors.add(blueprintModuleWebItemDescriptorFactory.createModuleDescriptor(blueprint, addon, plugin));
            descriptors.add(blueprintContentTemplateModuleDescriptorFactory.createModuleDescriptor(blueprint, addon, plugin));
            descriptors.add(blueprintModuleDescriptorFactory.createModuleDescriptor(blueprint, addon, plugin));
        }
        return descriptors;
    }
}
