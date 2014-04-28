package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.webdriver.utils.element.ElementConditions;
import com.atlassian.webdriver.utils.element.WebDriverPoller;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;

public class JiraAddWorkflowTransitionFunctionParamsPage extends RemotePluginEmbeddedTestPage
{
    @Inject
    private PageBinder pageBinder;

    @Inject private WebDriverPoller poller;

    public JiraAddWorkflowTransitionFunctionParamsPage(String addonKey, String moduleKey)
    {
        super(addonAndModuleKey(addonKey, moduleKey));
    }

    public void submitWorkflowParams()
    {
        poller.waitUntil(ElementConditions.isPresent(By.id("add_submit")), 5);
        driver.findElement(By.id("add_submit")).click();
    }


}
