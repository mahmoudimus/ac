package com.atlassian.plugin.remotable.test.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.plugin.remotable.test.RemoteWebPanels;
import com.atlassian.webdriver.AtlassianWebDriver;

import javax.inject.Inject;

/**
 * An user ViewProfile page that is expected to have a panel provided by a remote plugin.
 */
public class JiraViewProfilePage implements Page
{
    final private String userName;

    @Inject
    private AtlassianWebDriver driver;

    @ElementBy (xpath = RemoteWebPanels.REMOTE_WEB_PANELS_XPATH, pageElementClass = RemoteWebPanels.class)
    private RemoteWebPanels webPanels;

    public JiraViewProfilePage(String userName)
    {
        this.userName = userName;
    }

    @Override
    public String getUrl()
    {
        return "/secure/ViewProfile.jspa?name=" + userName;
    }

    public RemoteWebPanels getWebPanels()
    {
        return webPanels;
    }
}
