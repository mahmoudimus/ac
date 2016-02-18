package com.atlassian.plugin.connect.confluence.theme;

import com.atlassian.confluence.plugin.descriptor.LayoutModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
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

import java.util.List;
import java.util.stream.Collectors;

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
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;

    @Autowired
    public ConfluenceThemeModuleProviderImpl(PluginRetrievalService pluginRetrievalService,
                                             ConnectJsonSchemaValidator schemaValidator,
                                             ConfluenceThemeModuleDescriptorFactory themeDescriptorFactory,
                                             ConfluenceLayoutModuleFactory layoutModuleFactory,
                                             IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                             IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory)
    {
        super(pluginRetrievalService, schemaValidator);
        this.themeDescriptorFactory = themeDescriptorFactory;
        this.layoutModuleFactory = layoutModuleFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
    }

    @Override
    public ConnectModuleMeta<ConfluenceThemeModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<ConfluenceThemeModuleBean> modules, ConnectAddonBean addon)
    {
        Plugin plugin = pluginRetrievalService.getPlugin();
        List<ModuleDescriptor> descriptors = Lists.newArrayList();
        for (ConfluenceThemeModuleBean moduleBean : modules)
        {
            List<LayoutModuleDescriptor> layouts = makeLayouts(addon, plugin, moduleBean);
            descriptors.addAll(layouts);
//            layouts must come before the theme that uses them
            descriptors.add(themeDescriptorFactory.createModuleDescriptor(moduleBean, addon, plugin, layouts));
            for (UiOverrideBean overrides : moduleBean.getOverrides())
            {
                IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                                                                                        .addon(addon.getKey())
                                                                                        .module(moduleBean.getRawKey())
                                                                                        .genericBodyTemplate()
                                                                                        .urlTemplate(overrides.getUrl())
                                                                                        .ensureUniqueNamespace(false)
                                                                                        .dimensions("100%", "100%")
                                                                                        .sign(true)
                                                                                        .build();
                iFrameRenderStrategyRegistry.register(addon.getKey(), moduleBean.getRawKey(), overrides.getType(), renderStrategy);
            }
        }

        return descriptors;
    }

    private List<LayoutModuleDescriptor> makeLayouts(ConnectAddonBean addon, Plugin plugin, ConfluenceThemeModuleBean moduleBean)
    {
        return moduleBean.getOverrides()
                         .stream()
                         .map(uiOverrideBean ->
                              {
                                  LayoutType type = LayoutType.valueOf(uiOverrideBean.getType());
                                  return layoutModuleFactory.createModuleDescriptor(addon, plugin, moduleBean, type);
                              })
                         .collect(Collectors.toList());
    }
}
