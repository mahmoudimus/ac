package com.atlassian.connect.test.jira.pageobjects;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddOnPage;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Page with buttons for executing the javascript jira quick issue create
 */
public class RemoteQuickCreateIssueGeneralPage extends ConnectAddOnPage implements Page
{

    @Inject
    protected AtlassianWebDriver driver;

    public RemoteQuickCreateIssueGeneralPage(String addonKey, String moduleKey)
    {
        super(addonKey, moduleKey, true);
    }

    @Override
    public String getUrl()
    {
        return IframeUtils.iframeServletPath(addOnKey, pageElementKey);
    }

    public void launchQuickCreate()
    {
        runInFrame(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                driver.findElement(By.id("dialog")).click();
                return null;
            }
        });

    }

    public String getCreatedIssueSummary()
    {
        return getValue("summarytext");
    }

    public WebElement getQuickCreateDialog()
    {
        return driver.findElement(By.id("create-issue-dialog"));
    }

    public String logMessage()
    {
        return getValue("log");
    }

}