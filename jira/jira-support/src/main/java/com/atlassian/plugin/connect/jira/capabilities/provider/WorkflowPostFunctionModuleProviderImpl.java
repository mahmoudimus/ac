package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilder;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.jira.capabilities.descriptor.workflow.WorkflowPostFunctionModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleMeta;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.plugin.connect.jira.capabilities.provider.JiraTemplateHelper.workflowPostFunctionTemplate;

@JiraComponent
@ExportAsDevService
public class WorkflowPostFunctionModuleProviderImpl extends AbstractJiraConnectModuleProvider<WorkflowPostFunctionModuleBean>
        implements WorkflowPostFunctionModuleProvider
{

    private static final WorkflowPostFunctionModuleMeta META = new WorkflowPostFunctionModuleMeta();

    private final WorkflowPostFunctionModuleDescriptorFactory workflowPostFunctionFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    @Autowired
    public WorkflowPostFunctionModuleProviderImpl(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            WorkflowPostFunctionModuleDescriptorFactory workflowPostFunctionFactory,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        super(pluginRetrievalService, schemaValidator);
        this.workflowPostFunctionFactory = workflowPostFunctionFactory;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    @Override
    public ConnectModuleMeta<WorkflowPostFunctionModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<WorkflowPostFunctionModuleBean> modules, final ConnectModuleProviderContext moduleProviderContext)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();

        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();
        for (WorkflowPostFunctionModuleBean bean : modules)
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

            descriptors.add(beanToDescriptor(moduleProviderContext, pluginRetrievalService.getPlugin(), bean));
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
}
