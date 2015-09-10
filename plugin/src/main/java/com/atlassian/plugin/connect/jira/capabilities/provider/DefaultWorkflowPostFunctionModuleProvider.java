package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilder;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.workflow.WorkflowPostFunctionModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.spi.module.provider.AbstractConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.plugin.connect.jira.capabilities.provider.JiraTemplateHelper.workflowPostFunctionTemplate;

@JiraComponent
@ExportAsDevService
public class DefaultWorkflowPostFunctionModuleProvider extends AbstractConnectModuleProvider<WorkflowPostFunctionModuleBean>
        implements WorkflowPostFunctionModuleProvider
{
    public static final String DESCRIPTOR_KEY = "jiraWorkflowPostFunctions";
    public static final Class BEAN_CLASS = WorkflowPostFunctionModuleBean.class;
    
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
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin theConnectPlugin, List<WorkflowPostFunctionModuleBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();

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
                .template(workflowPostFunctionTemplate(resource))
                .urlTemplate(urlBean.getUrl())
                ;

        if (resource.equals(WorkflowPostFunctionResource.VIEW))
        {
            builder.ensureUniqueNamespace(true);
        }

        IFrameRenderStrategy renderStrategy = builder.build();

        iFrameRenderStrategyRegistry.register(addon.getKey(), bean.getRawKey(), resource.getResource(), renderStrategy);
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

    @Override
    public String getSchemaPrefix()
    {
        return "jira";
    }
}
