package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.plugin.connect.test.pageobjects.RemotePageUtil;

/**
 * Describes JIRA component tab
 */
public class JiraComponentTabPanel extends AbstractJiraTabPage
{
    public JiraComponentTabPanel(String projectKey, String componentId, String pluginKey, String moduleKey)
    {
        super(projectKey, componentId, pluginKey, moduleKey);
    }

    @Override
    public String getUrl()
    {
        return "/browse/" + projectKey + "/component/" + tabId;
    }

    public String getComponentId()
    {
        return RemotePageUtil.findInContext(iframeSrc, "component_id");
    }
}
