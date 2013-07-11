package com.atlassian.plugin.remotable.test.jira;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;
import org.openqa.selenium.By;

public class JiraAdministrationPage extends AbstractJiraPage
{
    private static final String JIRA_ADMIN_PAGE_URI = "/secure/admin/ViewApplicationProperties.jspa";
    private static final String JIRA_ADMIN_PAGE_SERVLET = "servlet-jira-admin-page";
    private static final String JIRA_ADMIN_PAGE_WEBITEM = "webitem-jira-admin-page";
    private static final String REMOTE_PLUGIN_ADMIN_KEY_SERVLET = "servlet-remotePluginAdmin";
    private static final String REMOTE_PLUGIN_ADMIN_KEY_WEBITEM = "webitem-remotePluginAdmin";

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
        return bindAdminPage(JIRA_ADMIN_PAGE_SERVLET);
    }

    public RemotePluginEmbeddedTestPage clickGeneralRemotableAdminPage()
    {
        return bindAdminPage(REMOTE_PLUGIN_ADMIN_KEY_SERVLET);
    }

    public boolean hasJiraRemotableAdminPageLink()
    {
        return hasLinkToAdminPage(JIRA_ADMIN_PAGE_WEBITEM);
    }

    public boolean hasGeneralRemotableAdminPage()
    {
        return hasLinkToAdminPage(REMOTE_PLUGIN_ADMIN_KEY_WEBITEM);
    }

    private boolean hasLinkToAdminPage(final String adminPageKey)
    {
        return findAdminPage(adminPageKey) != null;
    }

    private RemotePluginEmbeddedTestPage bindAdminPage(final String adminPageKey)
    {
        findAdminPage(adminPageKey).click();
        return pageBinder.bind(RemotePluginEmbeddedTestPage.class, adminPageKey);
    }

    private PageElement findAdminPage(final String adminPageKey)
    {
        return elementFinder.find(By.id(adminPageKey));
    }
}
