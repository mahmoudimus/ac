package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.pageobjects.RemoteXdmEventPanel;
import com.google.common.base.Optional;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * A ViewIssue page that is expected to have a panel provided by a remote plugin.
 */
public class JiraViewIssuePage implements Page
{
    private String issueKey;

    @Inject
    private com.atlassian.webdriver.AtlassianWebDriver driver;
    @Inject
    private PageBinder pageBinder;
    @Inject
    private PageElementFinder pageElementFinder;

    public JiraViewIssuePage(String issueKey)
    {
        this.issueKey = issueKey;
    }

    @Override
    public String getUrl()
    {
        return "/browse/" + issueKey;
    }

    public RemoteWebPanel findWebPanel(String panelId)
    {
        return pageBinder.bind(RemoteWebPanel.class, panelId);
    }

    public RemoteXdmEventPanel findXdmEventPanel(String addOnId, String moduleId)
    {
        return pageBinder.bind(RemoteXdmEventPanel.class, addOnId, moduleId);
    }

    public RemoteWebItem findWebItem(String webItemId, Optional<String> dropDownMenuId)
    {
        return pageBinder.bind(RemoteWebItem.class, webItemId, dropDownMenuId);
    }

    public boolean isTabPanelPresent(String id)
    {
        return pageElementFinder.find(By.id(id)).timed().isPresent().byDefaultTimeout();
    }

    public Section getSection(String moduleKey)
    {
        return pageBinder.bind(Section.class, moduleKey);
    }
}
