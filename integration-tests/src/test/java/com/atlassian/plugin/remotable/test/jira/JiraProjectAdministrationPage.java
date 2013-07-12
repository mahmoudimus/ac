package com.atlassian.plugin.remotable.test.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.plugin.remotable.test.RemoteWebPanels;

/**
 * Describes a project administration page.
 */
public class JiraProjectAdministrationPage implements Page
{
    private final String projectKey;

    @ElementBy (xpath = RemoteWebPanels.REMOTE_WEB_PANELS_XPATH, pageElementClass = RemoteWebPanels.class)
    private RemoteWebPanels webPanels;

    public JiraProjectAdministrationPage(String projectKey)
    {
        this.projectKey = projectKey;
    }

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/project-config/" + projectKey;
    }

    public RemoteWebPanels getWebPanels()
    {
        return webPanels;
    }
}

