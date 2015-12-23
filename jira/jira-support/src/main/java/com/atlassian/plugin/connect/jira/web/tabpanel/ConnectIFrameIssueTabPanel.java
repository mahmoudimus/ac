package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel3;
import com.atlassian.jira.plugin.issuetabpanel.GetActionsRequest;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.ShowPanelRequest;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.jira.web.context.IssueContextParameterMapper;
import com.atlassian.plugin.connect.jira.web.context.ProjectContextParameterMapper;
import com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import static com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager.CONTEXT_KEY_HELPER;
import static com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager.CONTEXT_KEY_USERNAME;
import static com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyUtil.renderToString;

public class ConnectIFrameIssueTabPanel extends AbstractIssueTabPanel3
{

    private IFrameRenderStrategy iFrameRenderStrategy;
    private PluggableParametersExtractor pluggableParametersExtractor;
    private IssueContextParameterMapper issueContextParameterMapper;
    private ProjectContextParameterMapper projectContextParameterMapper;

    public ConnectIFrameIssueTabPanel(IFrameRenderStrategy iFrameRenderStrategy,
            PluggableParametersExtractor pluggableParametersExtractor,
            IssueContextParameterMapper issueContextParameterMapper,
            ProjectContextParameterMapper projectContextParameterMapper)
    {
        this.iFrameRenderStrategy = iFrameRenderStrategy;
        this.pluggableParametersExtractor = pluggableParametersExtractor;
        this.issueContextParameterMapper = issueContextParameterMapper;
        this.projectContextParameterMapper = projectContextParameterMapper;
    }

    @Override
    public boolean showPanel(final ShowPanelRequest request)
    {
        Map<String, Object> context = createContext(request.issue(), request.remoteUser());
        return iFrameRenderStrategy.shouldShow(context);
    }

    @Override
    public List<IssueAction> getActions(final GetActionsRequest request)
    {
        Map<String, Object> context = createContext(request.issue(), request.remoteUser());
        Map<String, String> contextParameters = pluggableParametersExtractor.extractParameters(context);
        StringIssueAction stringAction = new StringIssueAction(renderToString(contextParameters, iFrameRenderStrategy));
        return Lists.newArrayList(stringAction);
    }

    private Map<String, Object> createContext(Issue issue, ApplicationUser applicationUser)
    {
        Project project = issue.getProjectObject();

        Map<String, Object> context = Maps.newHashMap();
        TypeBasedConnectContextParameterMapper.addContextEntry(issueContextParameterMapper, issue, context);
        TypeBasedConnectContextParameterMapper.addContextEntry(projectContextParameterMapper, project, context);
        context.put(CONTEXT_KEY_HELPER, new JiraHelper(ExecutingHttpRequest.get(), project, context));
        if (applicationUser != null)
        {
            context.put(CONTEXT_KEY_USERNAME, applicationUser.getUsername());
        }
        return context;
    }
}
