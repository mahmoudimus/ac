package com.atlassian.plugin.remotable.test.jira;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;
import org.openqa.selenium.By;

public class JiraAdministrationPage extends AbstractJiraPage
{
    private static final String URI = "/secure/admin/ViewApplicationProperties.jspa";
    private static final String JIRA_ADMIN_PAGE_KEY = "jira-admin-page";
    private static final String REMOTE_PLUGIN_ADMIN_KEY = "remotePluginAdmin";

    public JiraAdministrationPage()
    {
        super();
    }

    @Override
    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id("general_configuration")).timed().isPresent();
    }

    public RemotePluginEmbeddedTestPage clickJiraRemotableAdminPage()
    {
        return bindAdminPage(JIRA_ADMIN_PAGE_KEY);
    }

    public RemotePluginEmbeddedTestPage clickGeneralRemotableAdminPage()
    {
        return bindAdminPage(REMOTE_PLUGIN_ADMIN_KEY);
    }

    public boolean containsJiraRemotableAdminPageLink()
    {
        return containsLinkToAdminPage(JIRA_ADMIN_PAGE_KEY);
    }

    public boolean containsGeneralRemotableAdminPage()
    {
        return containsLinkToAdminPage(REMOTE_PLUGIN_ADMIN_KEY);
    }

    private boolean containsLinkToAdminPage(final String adminPageKey)
    {
        return elementFinder.find(By.id("webitem-" + adminPageKey)) != null;
    }

    private RemotePluginEmbeddedTestPage bindAdminPage(final String adminPageKey)
    {
        elementFinder.find(By.id("webitem-" + adminPageKey)).click();
        return pageBinder.bind(RemotePluginEmbeddedTestPage.class, "servlet-" + adminPageKey);
    }
}
