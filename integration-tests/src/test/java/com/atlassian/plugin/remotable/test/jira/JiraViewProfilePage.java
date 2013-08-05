package com.atlassian.plugin.remotable.test.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.remotable.test.pageobjects.RemoteWebPanel;
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

    @Inject
    private PageBinder pageBinder;

    public JiraViewProfilePage(String userName)
    {
        this.userName = userName;
    }

    @Override
    public String getUrl()
    {
        return "/secure/ViewProfile.jspa?name=" + userName;
    }

    public RemoteWebPanel findWebPanel(String panelId)
    {
        return pageBinder.bind(RemoteWebPanel.class, panelId);
    }
}
