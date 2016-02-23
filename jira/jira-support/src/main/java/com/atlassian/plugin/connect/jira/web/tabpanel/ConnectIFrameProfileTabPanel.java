package com.atlassian.plugin.connect.jira.web.tabpanel;

import java.util.Map;

import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.connect.api.web.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.jira.web.context.JiraModuleContextParameters;
import com.atlassian.plugin.connect.jira.web.context.JiraModuleContextParametersImpl;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableMap;

import static com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyUtil.renderAccessDeniedToString;
import static com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyUtil.renderToString;
import static com.atlassian.plugin.connect.spi.web.context.WebFragmentModuleContextExtractor.MODULE_CONTEXT_KEY;
import static java.util.Collections.emptyMap;

/**
 *
 */
public class ConnectIFrameProfileTabPanel implements ViewProfilePanel {
    private final IFrameRenderStrategy iFrameRenderStrategy;
    private final ModuleContextFilter moduleContextFilter;
    private final UserManager userManager;

    public ConnectIFrameProfileTabPanel(IFrameRenderStrategy iFrameRenderStrategy,
                                        ModuleContextFilter moduleContextFilter,
                                        UserManager userManager) {
        this.iFrameRenderStrategy = iFrameRenderStrategy;
        this.moduleContextFilter = moduleContextFilter;
        this.userManager = userManager;
    }

    @Override
    public void init(final ViewProfilePanelModuleDescriptor moduleDescriptor) {
    }

    public String getHtml(ApplicationUser user) {
        ModuleContextParameters unfilteredContext = createUnfilteredContext(user);
        Map<String, ModuleContextParameters> conditionContext = ImmutableMap.of(MODULE_CONTEXT_KEY, unfilteredContext);

        if (iFrameRenderStrategy.shouldShow(conditionContext)) {
            return renderToString(moduleContextFilter.filter(unfilteredContext), iFrameRenderStrategy);
        } else {
            return renderAccessDeniedToString(iFrameRenderStrategy);
        }
    }

    private ModuleContextParameters createUnfilteredContext(final ApplicationUser profileUser) {
        UserProfile userProfile = userManager.getUserProfile(new UserKey(profileUser.getKey()));
        JiraModuleContextParameters unfilteredContext = new JiraModuleContextParametersImpl(emptyMap());
        unfilteredContext.addProfileUser(userProfile);
        return unfilteredContext;
    }
}
