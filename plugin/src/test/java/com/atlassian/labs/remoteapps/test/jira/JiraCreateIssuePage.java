package com.atlassian.labs.remoteapps.test.jira;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.labs.remoteapps.test.GeneralPage;
import com.atlassian.labs.remoteapps.test.RemoteAppTestPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Not sure why this isn't in altassian-jira-pageobjects.
 */
public class JiraCreateIssuePage extends AbstractJiraPage

{
    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageBinder pageBinder;
    private final long projectId;

    @ElementBy(id = "issue-create")
    private PageElement createIssueForm;

    @FindBy(id = "summary")
    private WebElement summaryField;

    public JiraCreateIssuePage(long projectId)
    {
        this.projectId = projectId;
    }

    @Override
    public String getUrl()
    {
        return "/secure/CreateIssue.jspa?pid=" + projectId + "&issuetype=1&Create=Create";
    }

    public JiraCreateIssuePage summary(String summary)
    {
        summaryField.sendKeys(summary);
        return this;
    }

    public ViewIssuePage submit()
    {
        summaryField.submit();
        driver.waitUntilElementIsLocated(By.id("key-val"));
        String issueKey = driver.findElement(By.id("key-val")).getText();

//        return pageBinder.bind(JiraRemoteAppDecoratedViewIssuePage.class,
//                               pageBinder.bind(ViewIssuePage.class, issueKey),
//                               "viewissue-remoteAppViewIssue");

        return pageBinder.bind(ViewIssuePage.class, issueKey);
    }

    @Override
    public TimedCondition isAt()
    {
        return createIssueForm.timed().isPresent();

    }

}
