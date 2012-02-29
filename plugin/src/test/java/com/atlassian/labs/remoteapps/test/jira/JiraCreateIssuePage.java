package com.atlassian.labs.remoteapps.test.jira;

import javax.inject.Inject;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * There is no CreateIssuePage in altassian-jira-pageobjects, so we add one here.
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
        return pageBinder.bind(ViewIssuePage.class, issueKey);
    }

    @Override
    public TimedCondition isAt()
    {
        return createIssueForm.timed().isPresent();
    }

}
