package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.projectconfig.pageobjects.ProjectInfoLocator;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.timeout.DefaultTimeouts;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.webdriver.utils.by.ByJquery;
import com.atlassian.webdriver.utils.element.ElementConditions;
import com.atlassian.webdriver.utils.element.WebDriverPoller;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Created by cwhittington on 10/04/2014.
 */
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
