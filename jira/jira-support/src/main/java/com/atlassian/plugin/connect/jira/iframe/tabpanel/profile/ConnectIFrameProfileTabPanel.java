package com.atlassian.plugin.connect.jira.iframe.tabpanel.profile;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.plugin.connect.api.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.jira.iframe.context.JiraModuleContextParameters;
import com.atlassian.plugin.connect.jira.iframe.context.JiraModuleContextParametersImpl;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyUtil.renderAccessDeniedToString;
import static com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyUtil.renderToString;
import static com.atlassian.plugin.connect.spi.iframe.webpanel.WebFragmentModuleContextExtractor.MODULE_CONTEXT_KEY;

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

    public String getHtml(final User user)
    {
        return getHtml(ApplicationUsers.from(user));
    }

    public String getHtml(ApplicationUser user)
    {
        ModuleContextParameters unfilteredContext = createUnfilteredContext(user);
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

    private ModuleContextParameters createUnfilteredContext(final ApplicationUser profileUser)
    {
        UserProfile userProfile = userManager.getUserProfile(new UserKey(profileUser.getKey()));
        JiraModuleContextParameters unfilteredContext = new JiraModuleContextParametersImpl();
        unfilteredContext.addProfileUser(userProfile);
        return unfilteredContext;
    }

}
