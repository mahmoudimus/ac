package com.atlassian.plugin.remotable.test.jira;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;
import org.openqa.selenium.By;

public class JiraAdministrationPage extends AbstractJiraPage
{
    private static final String JIRA_ADMIN_PAGE_URI = "/secure/admin/ViewApplicationProperties.jspa";
    private static final String JIRA_ADMIN_PAGE_KEY = "jira-admin-page";
    private static final String REMOTE_PLUGIN_ADMIN_KEY = "remotePluginAdmin";
    private static final String WEBITEM_KEY = "webitem-";

    @Override
    public String getUrl()
    {
        return JIRA_ADMIN_PAGE_URI;
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

    public boolean hasJiraRemotableAdminPageLink()
    {
        return hasLinkToAdminPage(JIRA_ADMIN_PAGE_KEY);
    }

    public boolean hasGeneralRemotableAdminPage()
    {
        return hasLinkToAdminPage(REMOTE_PLUGIN_ADMIN_KEY);
    }

    private boolean hasLinkToAdminPage(final String adminPageKey)
    {
        return findAdminPage(adminPageKey) != null;
    }

    private RemotePluginEmbeddedTestPage bindAdminPage(final String adminPageKey)
    {
        findAdminPage(adminPageKey).click();
        return pageBinder.bind(RemotePluginEmbeddedTestPage.class, "servlet-" + adminPageKey);
    }

    private PageElement findAdminPage(final String adminPageKey)
    {
        return elementFinder.find(By.id(WEBITEM_KEY + adminPageKey));
    }
}
