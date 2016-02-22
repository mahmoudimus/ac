package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.api.web.iframe.ConnectIFrameServletPath;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.jira.AbstractJiraConnectModuleProvider;
import com.atlassian.plugin.connect.jira.web.condition.IsProjectAdminCondition;
import com.atlassian.plugin.connect.modules.beans.AddonUrlContext;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.api.lifecycle.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.atlassian.plugin.connect.jira.web.JiraTemplateHelper.projectAdminTabTemplate;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;

/**
 * Module Provider for a Connect Project Admin TabPanel Module. Note that there is actually no P2 module descriptor.
 * Instead it is modelled as a web-item plus a servlet
 */
@JiraComponent
public class ConnectProjectAdminTabPanelModuleProvider extends AbstractJiraConnectModuleProvider<ConnectProjectAdminTabPanelModuleBean> {

    private static final ConnectProjectAdminTabPanelModuleMeta META = new ConnectProjectAdminTabPanelModuleMeta();

    private static final String ADMIN_ACTIVE_TAB = "adminActiveTab";

    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final ConditionLoadingValidator conditionLoadingValidator;

    @Autowired
    public ConnectProjectAdminTabPanelModuleProvider(PluginRetrievalService pluginRetrievalService,
                                                     ConnectJsonSchemaValidator schemaValidator,
                                                     WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                                     IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                                     IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                                     ConditionLoadingValidator conditionLoadingValidator) {
        super(pluginRetrievalService, schemaValidator);
        this.webItemModuleDescriptorFactory = webItemModuleDescriptorFactory;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.conditionLoadingValidator = conditionLoadingValidator;
    }

    @Override
    public ConnectModuleMeta<ConnectProjectAdminTabPanelModuleBean> getMeta() {
        return META;
    }

    @Override
    public List<ConnectProjectAdminTabPanelModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException {
        List<ConnectProjectAdminTabPanelModuleBean> adminTabPanels = super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
        conditionLoadingValidator.validate(pluginRetrievalService.getPlugin(), descriptor, getMeta(), adminTabPanels);
        return adminTabPanels;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<ConnectProjectAdminTabPanelModuleBean> modules, ConnectAddonBean connectAddonBean) {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (ConnectProjectAdminTabPanelModuleBean bean : modules) {
            // render a web item for our tab
            WebItemModuleBean webItemModuleBean = newWebItemBean()
                    .withName(bean.getName())
                    .withKey(bean.getRawKey())
                    .withUrl(ConnectIFrameServletPath.forModule(connectAddonBean.getKey(), bean.getRawKey()))
                    .withContext(AddonUrlContext.page)
                    .withLocation(bean.getAbsoluteLocation())
                    .withWeight(bean.getWeight())
                    .withConditions(bean.getConditions())
                    .setNeedsEscaping(false)
                    .build();

            builder.add(webItemModuleDescriptorFactory.createModuleDescriptor(
                    webItemModuleBean, connectAddonBean, pluginRetrievalService.getPlugin(), IsProjectAdminCondition.class));

            // register a render strategy for the servlet backing our iframe tab
            IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addon(connectAddonBean.getKey())
                    .module(bean.getKey(connectAddonBean))
                    .template(projectAdminTabTemplate())
                    .urlTemplate(bean.getUrl())
                    .additionalRenderContext(ADMIN_ACTIVE_TAB, bean.getKey(connectAddonBean))
                    .conditions(bean.getConditions())
                    .conditionClass(IsProjectAdminCondition.class)
                    .title(bean.getDisplayName())
                    .build();

            iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), bean.getRawKey(), renderStrategy);
        }

        return builder.build();
    }
}
