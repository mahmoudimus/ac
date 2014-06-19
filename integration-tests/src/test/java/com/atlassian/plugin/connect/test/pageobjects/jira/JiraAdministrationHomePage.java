package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import org.openqa.selenium.By;

public class JiraAdministrationHomePage extends AbstractJiraPage
{
    private static final String JIRA_ADMIN_PAGE_URI = "/secure/admin/ViewApplicationProperties.jspa";
    private static final String JIRA_ADMIN_PAGE_SERVLET = "jira-admin-page";
    private static final String JIRA_ADMIN_PAGE_WEBITEM = "jira-admin-page";
    private static final String REMOTE_PLUGIN_ADMIN_KEY_SERVLET = "remotePluginAdmin";
    private static final String REMOTE_PLUGIN_ADMIN_KEY_WEBITEM = "remotePluginAdmin";

    private final String extraPrefix;
    private final String addOnKey;

    public JiraAdministrationHomePage()
    {
        this("", "");
    }

    @Deprecated // used to provide legacy ID prefixes for modules provided by XML add-ons
    @XmlDescriptor
    public JiraAdministrationHomePage(String extraPrefix)
    {
        this(extraPrefix, "");
    }

    public JiraAdministrationHomePage(String extraPrefix, String addOnKey)
    {
        this.extraPrefix = extraPrefix;
        this.addOnKey = addOnKey;
    }

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

    public ConnectAddOnEmbeddedTestPage clickJiraRemotableAdminPage()
    {
        return bindAdminPage(JIRA_ADMIN_PAGE_WEBITEM, JIRA_ADMIN_PAGE_SERVLET);
    }

    public ConnectAddOnEmbeddedTestPage clickGeneralRemotableAdminPage()
    {
        return bindAdminPage(REMOTE_PLUGIN_ADMIN_KEY_WEBITEM, REMOTE_PLUGIN_ADMIN_KEY_SERVLET);
    }

    public boolean hasJiraRemotableAdminPageLink()
    {
        return hasLinkToAdminPage(JIRA_ADMIN_PAGE_WEBITEM);
    }

    public boolean hasGeneralRemotableAdminPage()
    {
        return hasLinkToAdminPage(REMOTE_PLUGIN_ADMIN_KEY_WEBITEM);
    }

    private boolean hasLinkToAdminPage(final String adminPageWebItemKey)
    {
        return findAdminPageLink(constructAddOnAdminPageLinkId(adminPageWebItemKey)).isPresent();
    }

    private ConnectAddOnEmbeddedTestPage bindAdminPage(final String adminPageWebItemKey, final String adminPageServletKey)
    {
        findAdminPageLink(constructAddOnAdminPageLinkId(adminPageWebItemKey)).click();
        return pageBinder.bind(ConnectAddOnEmbeddedTestPage.class, adminPageServletKey, addOnKey, true);
    }

    private String constructAddOnAdminPageLinkId(String adminPageWebItemKey)
    {
        return addOnKey + "__" + adminPageWebItemKey;
    }

    private PageElement findAdminPageLink(final String adminPageKey)
    {
        return elementFinder.find(By.id(adminPageKey));
    }
}
