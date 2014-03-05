package com.atlassian.plugin.connect.plugin.iframe.tabpanel.profile;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextParametersImpl;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyUtil.renderAccessDeniedToString;
import static com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyUtil.renderToString;
import static com.atlassian.plugin.connect.plugin.iframe.webpanel.WebFragmentModuleContextExtractor.MODULE_CONTEXT_KEY;

/**
 *
 */
public class ConnectIFrameProfileTabPanel implements ViewProfilePanel
{
    private final IFrameRenderStrategy iFrameRenderStrategy;
    private final ModuleContextFilter moduleContextFilter;
    private final UserManager userManager;

    public ConnectIFrameProfileTabPanel(IFrameRenderStrategy iFrameRenderStrategy,
            ModuleContextFilter moduleContextFilter,
            UserManager userManager)
    {
        this.iFrameRenderStrategy = iFrameRenderStrategy;
        this.moduleContextFilter = moduleContextFilter;
        this.userManager = userManager;
    }

    @Override
    public void init(final ViewProfilePanelModuleDescriptor moduleDescriptor)
    {
    }

    @Override
    public String getHtml(final User profileUser)
    {
        ModuleContextParameters unfilteredContext = createUnfilteredContext(profileUser);
        Map<String, ModuleContextParameters> conditionContext = ImmutableMap.of(MODULE_CONTEXT_KEY, unfilteredContext);

        if (iFrameRenderStrategy.shouldShow(conditionContext))
        {
            return renderToString(moduleContextFilter.filter(unfilteredContext), iFrameRenderStrategy);
        }
        else
        {
            return renderAccessDeniedToString(iFrameRenderStrategy);
        }
    }

    private ModuleContextParameters createUnfilteredContext(final User profileUser)
    {
        UserProfile userProfile = userManager.getUserProfile(profileUser.getName());
        JiraModuleContextParameters unfilteredContext = new JiraModuleContextParametersImpl();
        unfilteredContext.addProfileUser(userProfile);
        return unfilteredContext;
    }

}
