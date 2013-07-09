package com.atlassian.plugin.remotable.test;

import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.confluence.PageIdWebPanelParameterExtractor;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.confluence.SpaceIdWebPanelParameterExtractor;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira.IssueIdWebPanelParameterExtractor;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira.ProjectIdWebPanelParameterExtractor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A remotable web-panel that is expected to contain some test values.
 */
public class RemoteWebPanel
{
    private final RemotePluginEmbeddedTestPage remotePluginEmbeddedTestPage;

    public RemoteWebPanel(final RemotePluginEmbeddedTestPage remotePluginEmbeddedTestPage)
    {
        this.remotePluginEmbeddedTestPage = checkNotNull(remotePluginEmbeddedTestPage);
    }

    public String getUserId()
    {
        return remotePluginEmbeddedTestPage.waitForValue("user_id");
    }

    public String getProjectId()
    {
        return remotePluginEmbeddedTestPage.waitForValue(ProjectIdWebPanelParameterExtractor.PROJECT_ID);
    }

    public String getIssueId()
    {
        return remotePluginEmbeddedTestPage.waitForValue(IssueIdWebPanelParameterExtractor.ISSUE_ID);
    }

    public String getSpaceId()
    {
        return remotePluginEmbeddedTestPage.waitForValue(SpaceIdWebPanelParameterExtractor.SPACE_ID);
    }

    public String getPageId()
    {
        return remotePluginEmbeddedTestPage.waitForValue(PageIdWebPanelParameterExtractor.PAGE_ID);
    }
}
