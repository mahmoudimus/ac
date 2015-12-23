package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.jira.web.context.JiraProfileUserContextParameterMapper;
import com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyUtil.renderAccessDeniedToString;
import static com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyUtil.renderToString;

public class ConnectIFrameProfileTabPanel implements ViewProfilePanel
{

    private IFrameRenderStrategy iFrameRenderStrategy;
    private PluggableParametersExtractor pluggableParametersExtractor;
    private JiraProfileUserContextParameterMapper profileUserContextParameterMapper;

    public ConnectIFrameProfileTabPanel(IFrameRenderStrategy iFrameRenderStrategy,
            PluggableParametersExtractor pluggableParametersExtractor,
            JiraProfileUserContextParameterMapper profileUserContextParameterMapper)
    {
        this.iFrameRenderStrategy = iFrameRenderStrategy;
        this.pluggableParametersExtractor = pluggableParametersExtractor;
        this.profileUserContextParameterMapper = profileUserContextParameterMapper;
    }

    @Override
    public void init(final ViewProfilePanelModuleDescriptor moduleDescriptor)
    {}

    public String getHtml(ApplicationUser user)
    {
        Map<String, Object> context = createContext(user);
        if (iFrameRenderStrategy.shouldShow(context))
        {
            Map<String, String> contextParameters = pluggableParametersExtractor.extractParameters(context);
            return renderToString(contextParameters, iFrameRenderStrategy);
        }
        else
        {
            return renderAccessDeniedToString(iFrameRenderStrategy);
        }
    }

    private Map<String, Object> createContext(ApplicationUser user)
    {
        Map<String, Object> context = new HashMap<>();
        TypeBasedConnectContextParameterMapper.addContextEntry(profileUserContextParameterMapper, user, context);
        return context;
    }
}
