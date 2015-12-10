package com.atlassian.plugin.connect.confluence.theme;

import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.confluence.AbstractConfluenceConnectModuleProvider;
import com.atlassian.plugin.connect.modules.beans.ConfluenceThemeModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.nested.UiOverrideBean;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;

import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@ConfluenceComponent
@ExportAsDevService
public class ConfluenceThemeModuleProviderImpl extends AbstractConfluenceConnectModuleProvider<ConfluenceThemeModuleBean> implements ConfluenceThemeModuleProvider
{
    private static final ConfuenceThemeMeta META = new ConfuenceThemeMeta();

    private final ConfluenceThemeModuleDescriptorFactory themeDescriptorFactory;
    private final ConfluenceLayoutModuleFactory layoutModuleFactory;

    @Autowired
    public ConfluenceThemeModuleProviderImpl(PluginRetrievalService pluginRetrievalService,
                                             ConnectJsonSchemaValidator schemaValidator,
                                             ConfluenceThemeModuleDescriptorFactory themeDescriptorFactory,
                                             ConfluenceLayoutModuleFactory layoutModuleFactory)
    {
        super(pluginRetrievalService, schemaValidator);
        this.themeDescriptorFactory = themeDescriptorFactory;
        this.layoutModuleFactory = layoutModuleFactory;
    }

    @Override
    public ConnectModuleMeta<ConfluenceThemeModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<ConfluenceThemeModuleBean> modules,
                                                                ConnectAddonBean addon)
    {
        Plugin plugin = pluginRetrievalService.getPlugin();
        List<ModuleDescriptor> descriptors = Lists.newArrayList();
        for (ConfluenceThemeModuleBean moduleBean : modules)
        {
            descriptors.add(themeDescriptorFactory.createModuleDescriptor(moduleBean, addon, plugin));
            for (UiOverrideBean uiOverrideBean : moduleBean.getOverrides())
            {
                descriptors.add(layoutModuleFactory.createModuleDescriptor(addon, plugin, moduleBean, LayoutType.valueOf(uiOverrideBean.getType())));
            }
        }
        return descriptors;
    }
}
