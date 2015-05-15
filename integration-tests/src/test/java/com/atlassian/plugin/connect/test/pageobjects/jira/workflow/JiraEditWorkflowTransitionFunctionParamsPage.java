package com.atlassian.plugin.connect.test.pageobjects.jira.workflow;

import com.atlassian.pageobjects.elements.timeout.DefaultTimeouts;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.webdriver.utils.element.WebDriverPoller;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.webdriver.utils.element.ElementConditions.isPresent;
import static com.atlassian.webdriver.utils.element.ElementConditions.isVisible;

public class JiraEditWorkflowTransitionFunctionParamsPage extends ConnectAddOnEmbeddedTestPage
{
    @Inject private WebDriverPoller poller;

    public JiraEditWorkflowTransitionFunctionParamsPage(String addonKey, String moduleKey)
    {
        super(addonKey, moduleKey, true);
    }

    public void submit()
    {
        By submitButtonLocator = By.id("add_submit");
        // waits for form to load to make sure that clicking on submit will work in chrome
        poller.waitUntil(isPresent(By.name("jiraform")), DefaultTimeouts.DEFAULT_PAGE_LOAD);
        poller.waitUntil(isVisible(submitButtonLocator), 10);
        driver.findElement(submitButtonLocator).click();
    }
}
