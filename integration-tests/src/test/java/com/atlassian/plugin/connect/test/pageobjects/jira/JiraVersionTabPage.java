package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.plugin.connect.test.pageobjects.RemotePageUtil;

/**
 * Describes JIRA version tab
 */
public class JiraVersionTabPage extends AbstractJiraTabPage
{
    public JiraVersionTabPage(String projectKey, String versionId, String pluginKey, String moduleKey)
    {
        super(projectKey, versionId, pluginKey, moduleKey);
    }

    @Override
    public String getUrl()
    {
        return "/browse/" + projectKey + "/fixforversion/" + tabId;
    }

    public String getVersionId()
    {
        return RemotePageUtil.findInContext(iframeSrc, "version_id");
    }
}
