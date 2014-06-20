package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElementFinder;
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
    private String extraPrefix;

    @Inject
    private com.atlassian.webdriver.AtlassianWebDriver driver;
    @Inject
    private PageBinder pageBinder;
    @Inject
    private PageElementFinder pageElementFinder;

    public JiraViewIssuePage(String issueKey)
    {
        this(issueKey, "");
    }

    @Deprecated // takes an extra ID prefix for modules provided by XML modules
    public JiraViewIssuePage(String issueKey, String extraPrefix)
    {
        this.issueKey = issueKey;
        this.extraPrefix = extraPrefix;
    }

    @Override
    public String getUrl()
    {
        return "/browse/" + issueKey;
    }

    public void addLabelViaInlineEdit(String label)
    {
        driver.waitUntilElementIsVisible(By.cssSelector(".editable-field .labels"));
        driver.findElement(By.className("labels")).click();
        driver.waitUntilElementIsVisible(By.id("labels-textarea"));
        driver.findElement(By.id("labels-textarea")).sendKeys(label + "\t");
        driver.waitUntilElementIsVisible(By.cssSelector("#labels-form .submit"));
        driver.findElement(By.cssSelector("#labels-form .submit")).click();
        driver.waitUntilElementIsVisible(By.className("labels"));
    }

    public RemoteWebPanel findWebPanel(String panelId)
    {
        return pageBinder.bind(RemoteWebPanel.class, panelId, extraPrefix);
    }

    public RemoteXdmEventPanel findXdmEventPanel(String panelId)
    {
        return pageBinder.bind(RemoteXdmEventPanel.class, panelId);
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
