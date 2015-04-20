package com.atlassian.plugin.connect.test.pageobjects.jira.workflow;

import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.webdriver.utils.element.ElementConditions;
import com.atlassian.webdriver.utils.element.WebDriverPoller;
import org.openqa.selenium.By;

import javax.inject.Inject;

public class JiraEditWorkflowTransitionFunctionParamsPage extends ConnectAddOnEmbeddedTestPage
{
    @Inject private WebDriverPoller poller;

    public JiraEditWorkflowTransitionFunctionParamsPage(String addonKey, String moduleKey)
    {
        super(addonKey, moduleKey, true);
    }

    public void submit()
    {
        poller.waitUntil(ElementConditions.isPresent(By.id("add_submit")), 10);
        driver.findElement(By.id("add_submit")).click();
    }
}
