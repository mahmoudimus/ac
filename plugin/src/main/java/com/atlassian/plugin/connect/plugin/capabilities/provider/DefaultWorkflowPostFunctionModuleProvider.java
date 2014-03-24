package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.workflow.WorkflowPostFunctionModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import org.springframework.beans.factory.annotation.Autowired;

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
    public List<ModuleDescriptor> provideModules(ConnectAddonBean addon, Plugin theConnectPlugin, String jsonFieldName, List<WorkflowPostFunctionModuleBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (WorkflowPostFunctionModuleBean bean : beans)
        {
            // register render strategies for iframe workflow views
            if (bean.hasCreate())
            {
                registerIFrameRenderStrategy(addon, bean, JiraWorkflowPluginConstants.RESOURCE_NAME_INPUT_PARAMETERS, bean.getCreate());
            }
            if (bean.hasEdit())
            {
                registerIFrameRenderStrategy(addon, bean, JiraWorkflowPluginConstants.RESOURCE_NAME_EDIT_PARAMETERS, bean.getEdit());
            }
            if (bean.hasView())
            {
                registerIFrameRenderStrategy(addon, bean, JiraWorkflowPluginConstants.RESOURCE_NAME_VIEW, bean.getView());
            }

            descriptors.add(beanToDescriptor(addon, theConnectPlugin, bean));
        }

        return descriptors;
    }

    private ModuleDescriptor beanToDescriptor(ConnectAddonBean addon, Plugin theConnectPlugin, WorkflowPostFunctionModuleBean bean)
    {
        return workflowPostFunctionFactory.createModuleDescriptor(addon, theConnectPlugin, bean);
    }

    private void registerIFrameRenderStrategy(ConnectAddonBean addon, WorkflowPostFunctionModuleBean bean, String classifier, UrlBean urlBean)
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(addon.getKey())
                .module(bean.getRawKey())
                .workflowPostFunctionTemplate()
                .urlTemplate(urlBean.getUrl())
                .build();

        iFrameRenderStrategyRegistry.register(addon.getKey(), bean.getRawKey(), classifier, renderStrategy);
    }
}
