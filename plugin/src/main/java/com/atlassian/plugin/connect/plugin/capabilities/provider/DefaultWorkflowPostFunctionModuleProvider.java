package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.workflow.WorkflowPostFunctionModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilder;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@JiraComponent
@ExportAsDevService
public class DefaultWorkflowPostFunctionModuleProvider implements WorkflowPostFunctionModuleProvider
{
    private final WorkflowPostFunctionModuleDescriptorFactory workflowPostFunctionFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    @Autowired
    public DefaultWorkflowPostFunctionModuleProvider(WorkflowPostFunctionModuleDescriptorFactory workflowPostFunctionFactory,
                                                     IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                                     IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        this.workflowPostFunctionFactory = workflowPostFunctionFactory;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, String jsonFieldName, List<WorkflowPostFunctionModuleBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();
        for (WorkflowPostFunctionModuleBean bean : beans)
        {
            // register render strategies for iframe workflow views
            if (bean.hasCreate())
            {
                registerIFrameRenderStrategy(connectAddonBean, bean, WorkflowPostFunctionResource.CREATE, bean.getCreate());
            }
            if (bean.hasEdit())
            {
                registerIFrameRenderStrategy(connectAddonBean, bean, WorkflowPostFunctionResource.EDIT, bean.getEdit());
            }
            if (bean.hasView())
            {
                registerIFrameRenderStrategy(connectAddonBean, bean, WorkflowPostFunctionResource.VIEW, bean.getView());
            }

            descriptors.add(beanToDescriptor(moduleProviderContext, theConnectPlugin, bean));
        }

        return descriptors;
    }

    private ModuleDescriptor beanToDescriptor(ConnectModuleProviderContext addon, Plugin theConnectPlugin, WorkflowPostFunctionModuleBean bean)
    {
        return workflowPostFunctionFactory.createModuleDescriptor(addon, theConnectPlugin, bean);
    }

    private void registerIFrameRenderStrategy(ConnectAddonBean addon, WorkflowPostFunctionModuleBean bean, WorkflowPostFunctionResource resource, UrlBean urlBean)
    {
        IFrameRenderStrategyBuilder.InitializedBuilder builder = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(addon.getKey())
                .module(bean.getKey(addon))
                .workflowPostFunctionTemplate(resource)
                .urlTemplate(urlBean.getUrl());

        if (resource.equals(WorkflowPostFunctionResource.VIEW))
        {
            builder.ensureUniqueNamespace(true);
        }

        IFrameRenderStrategy renderStrategy = builder.build();

        iFrameRenderStrategyRegistry.register(addon.getKey(), bean.getRawKey(), resource.getResource(), renderStrategy);
    }
}
