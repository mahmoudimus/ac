package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.webdriver.utils.element.ElementConditions;
import com.atlassian.webdriver.utils.element.WebDriverPoller;
import org.openqa.selenium.By;

import javax.inject.Inject;

public class JiraAddWorkflowTransitionFunctionParamsPage extends ConnectAddOnEmbeddedTestPage
{
    @Inject private WebDriverPoller poller;

    public JiraAddWorkflowTransitionFunctionParamsPage(String addonKey, String moduleKey)
    {
        super(addonKey, moduleKey, true);
    }

    public void submitWorkflowParams()
    {
        poller.waitUntil(ElementConditions.isPresent(By.id("add_submit")), 5);
        driver.findElement(By.id("add_submit")).click();
    }
}
