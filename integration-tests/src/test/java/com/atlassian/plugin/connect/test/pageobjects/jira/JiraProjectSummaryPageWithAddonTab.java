package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.projects.pageobjects.webdriver.page.SummaryPage;
import com.atlassian.jira.projects.pageobjects.webdriver.page.sidebar.Sidebar;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.utils.WebItemUtils;

/**
 * A page object for the JIRA project summary page with a project tab panel provided by a Connect add-on.
 */
public class JiraProjectSummaryPageWithAddonTab extends SummaryPage
{

    private String projectKey;
    private String addonKey;
    private String moduleKey;

    public JiraProjectSummaryPageWithAddonTab(String projectKey, String addonKey, String moduleKey)
    {
        super(projectKey);
        this.projectKey = projectKey;
        this.addonKey = addonKey;
        this.moduleKey = moduleKey;
    }

    public JiraProjectSummaryPageWithAddonTab expandAddonsList()
    {
        Sidebar.SidebarLink addonsLink = getSidebarAddonsLink();
        return addonsLink.click(JiraProjectSummaryPageWithAddonTab.class, projectKey, addonKey, moduleKey);
    }

    public ConnectAddOnEmbeddedTestPage goToEmbeddedTestPageAddon()
    {
        Sidebar.SidebarLink addonLink = getAddonLink();
        return addonLink.click(ConnectAddOnEmbeddedTestPage.class, addonKey, moduleKey, true);
    }

    private Sidebar.SidebarLink getSidebarAddonsLink()
    {
        return getSidebar().getLinkById("com.atlassian.jira.jira-projects-plugin:tab-panel-link-parent");
    }

    private Sidebar.SidebarLink getAddonLink()
    {
        String linkId = WebItemUtils.linkId(ModuleKeyUtils.addonAndModuleKey(addonKey, moduleKey));
        return getSidebar().getLinkById(linkId);
    }
}
