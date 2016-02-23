package com.atlassian.connect.test.jira.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonPage;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;

/**
 * Page with buttons for executing the javascript jira quick issue create
 */
public class RemoteQuickCreateIssueGeneralPage extends ConnectAddonPage implements Page {

    @Inject
    protected AtlassianWebDriver driver;

    public RemoteQuickCreateIssueGeneralPage(String addonKey, String moduleKey) {
        super(addonKey, moduleKey, true);
    }

    @Override
    public String getUrl() {
        return IframeUtils.iframeServletPath(addonKey, pageElementKey);
    }

    public void launchQuickCreate() {
        runInFrame(() -> {
            driver.findElement(By.id("dialog")).click();
            return null;
        });

    }

    public String getCreatedIssueSummary() {
        return getValue("summarytext");
    }

    public WebElement getQuickCreateDialog() {
        return driver.findElement(By.id("create-issue-dialog"));
    }

    public String logMessage() {
        return getValue("log");
    }

}
