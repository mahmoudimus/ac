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

import static com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyUtil.renderToString;

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
        UserProfile userProfile = userManager.getUserProfile(profileUser.getName());
        // parse and filter module context
        JiraModuleContextParameters unfilteredContext = new JiraModuleContextParametersImpl();
        unfilteredContext.addProfileUser(userProfile);

        ModuleContextParameters filteredContext = moduleContextFilter.filter(unfilteredContext);

        // render tab HTML
        return renderToString(filteredContext, iFrameRenderStrategy);
    }

}
