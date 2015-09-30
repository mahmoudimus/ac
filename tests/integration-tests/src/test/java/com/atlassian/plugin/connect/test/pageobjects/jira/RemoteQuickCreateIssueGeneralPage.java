package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.test.utils.IframeUtils;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.Callable;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnPage;
import org.openqa.selenium.WebElement;

/**
 * Page with buttons for executing the javascript jira quick issue create
 */
public class RemoteQuickCreateIssueGeneralPage extends ConnectAddOnPage implements Page
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

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