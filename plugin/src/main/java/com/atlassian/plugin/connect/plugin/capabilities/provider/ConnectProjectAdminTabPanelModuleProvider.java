package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.ConnectIFrameServlet;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRequestProcessor;
import com.atlassian.plugin.connect.plugin.module.jira.conditions.IsProjectAdminCondition;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Module Provider for a Connect Project Admin TabPanel Module. Note that there is actually no P2 module descriptor.
 * Instead it is modelled as a web-item plus a servlet
 */
@JiraComponent
public class ConnectProjectAdminTabPanelModuleProvider
        implements ConnectModuleProvider<ConnectProjectAdminTabPanelModuleBean>
{
    public static final String PROJECT_ADMIN_TAB_PANELS = "jiraProjectAdminTabPanels";

    private static final String TEMPLATE_SUFFIX = "-project-admin";
    private static final String ADMIN_ACTIVE_TAB = "adminActiveTab";
    private static final String PROJECT_KEY = "project.key";

    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final JiraAuthenticationContext authenticationContext;

    @Autowired
    public ConnectProjectAdminTabPanelModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            JiraAuthenticationContext authenticationContext,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        this.authenticationContext = authenticationContext;
        this.webItemModuleDescriptorFactory = checkNotNull(webItemModuleDescriptorFactory);
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName, List<ConnectProjectAdminTabPanelModuleBean> beans)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (ConnectProjectAdminTabPanelModuleBean bean : beans)
        {
            // render a web item for our tab
            String webItemUri = ConnectIFrameServlet.iFrameServletPath(plugin.getKey(), bean.getKey());
            // we can't pass projectKey as an "extra param" to relativeAddOnUrlConverter as it will encode the {}
            webItemUri = appendProjectKeyParam(webItemUri);

            WebItemModuleBean webItemModuleBean = newWebItemBean()
                    .withName(bean.getName())
                    .withKey(bean.getKey())
                    .withUrl(webItemUri)
                    .withLocation(bean.getAbsoluteLocation())
                    .withWeight(bean.getWeight())
                    .build();

            builder.add(webItemModuleDescriptorFactory.createModuleDescriptor(plugin, addonBundleContext, webItemModuleBean));

            // register a render strategy for the servlet backing our iframe tab
            IsProjectAdminCondition projectAdminCondition = new IsProjectAdminCondition(checkNotNull(authenticationContext));
            IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addOn(plugin.getKey())
                    .module(bean.getKey())
                    .template("velocity/iframe-page" + TEMPLATE_SUFFIX + ".vm")
                    .urlTemplate(bean.getUrl())
                    .additionalRenderContext(ADMIN_ACTIVE_TAB, bean.getKey())
                    .condition(projectAdminCondition)
                    .title(bean.getDisplayName())
                    .requestPreprocessor(new JRA26407Workaround(projectAdminCondition))
                    .build();

            iFrameRenderStrategyRegistry.register(plugin.getKey(), bean.getKey(), renderStrategy);
        }

        return builder.build();
    }

    private String appendProjectKeyParam(final String relativeUri)
    {
        return String.format("%s?%s={%s}", relativeUri, PROJECT_KEY, PROJECT_KEY);
    }

    private static class JRA26407Workaround implements IFrameRequestProcessor
    {
        private final IsProjectAdminCondition projectAdminCondition;

        private JRA26407Workaround(IsProjectAdminCondition projectAdminCondition)
        {
            this.projectAdminCondition = projectAdminCondition;
        }

        @Override
        public void process(final HttpServletRequest req)
        {
            final Project project = getProject(req);
            req.setAttribute("com.atlassian.jira.projectconfig.util.ServletRequestProjectConfigRequestCache:project", project);

            // This is a workaround for JRA-26407.
            projectAdminCondition.setProject(project);
        }

        private Project getProject(final HttpServletRequest request)
        {
            Object projectKey = request.getParameterMap().get(PROJECT_KEY);
            if (projectKey instanceof String[])
            {
                final String key = ((String[]) projectKey)[0];
                return ComponentAccessor.getProjectManager().getProjectObjByKey(key);
            }
            throw new IllegalStateException("No " + PROJECT_KEY + " parameter found in the query string!");
        }
    }

}
