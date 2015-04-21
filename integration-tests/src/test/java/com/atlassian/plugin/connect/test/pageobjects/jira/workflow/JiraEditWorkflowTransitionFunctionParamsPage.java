package com.atlassian.plugin.connect.test.pageobjects.jira.workflow;

import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.webdriver.utils.element.ElementConditions;
import com.atlassian.webdriver.utils.element.WebDriverPoller;
import org.openqa.selenium.By;

import javax.inject.Inject;

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
        poller.waitUntil(isVisible(submitButtonLocator), 10);
        driver.findElement(submitButtonLocator).click();
    }
}
