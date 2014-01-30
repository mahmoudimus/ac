package com.atlassian.plugin.connect.test.pageobjects.jira;

import javax.inject.Inject;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;

import com.atlassian.plugin.connect.test.pageobjects.RemoteXdmEventPanel;
import org.openqa.selenium.By;

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

    public JiraViewIssuePage(String issueKey)
    {
        this.issueKey = issueKey;
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
        return pageBinder.bind(RemoteWebPanel.class, panelId);
    }

    public RemoteWebPanel findWebPanelFromXMLAddOn(String panelId)
    {
        return pageBinder.bind(RemoteWebPanel.class, panelId, "remote-web-panel-");
    }

    public RemoteXdmEventPanel findXdmEventPanel(String panelId)
    {
        return pageBinder.bind(RemoteXdmEventPanel.class, panelId);
    }
}
