package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.webdriver.utils.element.ElementConditions;
import com.atlassian.webdriver.utils.element.WebDriverPoller;
import org.openqa.selenium.By;

import javax.inject.Inject;

public class JiraAddWorkflowTransitionFunctionParamsPage extends RemotePluginEmbeddedTestPage
{
    @Inject
    private PageBinder pageBinder;

    @Inject private WebDriverPoller poller;

    public JiraAddWorkflowTransitionFunctionParamsPage(String moduleKey)
    {
        super(moduleKey);
    }

    public void submitWorkflowParams()
    {
        poller.waitUntil(ElementConditions.isPresent(By.id("add_submit")), 5);
        driver.findElement(By.id("add_submit")).click();
    }


}
