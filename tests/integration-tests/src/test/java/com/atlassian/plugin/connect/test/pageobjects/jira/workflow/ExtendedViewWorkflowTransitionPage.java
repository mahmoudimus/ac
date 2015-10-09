package com.atlassian.plugin.connect.test.pageobjects.jira.workflow;

import com.atlassian.jira.pageobjects.pages.admin.workflow.AddWorkflowTransitionPostFunctionPage;
import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowTransitionPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.utils.element.WebDriverPoller;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.webdriver.utils.element.ElementConditions.isPresent;
import static com.atlassian.webdriver.utils.element.ElementConditions.isVisible;

public class ExtendedViewWorkflowTransitionPage extends ViewWorkflowTransitionPage
{

    @Inject
    private PageBinder pageBinder;

    @Inject
    private WebDriverPoller poller;

    public ExtendedViewWorkflowTransitionPage(String workflowMode, String workflowName, String workflowStep, String workflowTransition)
    {
        super(workflowMode, workflowName, workflowStep, workflowTransition);
    }

    public JiraEditWorkflowTransitionFunctionParamsPage updateFirstPostFunction(String addonKey, String moduleKey)
    {
        By viewPostFunctionsTabLocator = By.id("view_post_functions");
        poller.waitUntil(isVisible(viewPostFunctionsTabLocator), 20);
        driver.findElement(viewPostFunctionsTabLocator).click();

        By editPostFunctionLocator = By.className("criteria-post-function-edit");
        poller.waitUntil(isPresent(editPostFunctionLocator), 10);
        driver.findElement(editPostFunctionLocator).click();

        return pageBinder.bind(JiraEditWorkflowTransitionFunctionParamsPage.class, addonKey, moduleKey);
    }

    public JiraEditWorkflowTransitionFunctionParamsPage goToAddAddonPostFunction(String postFunctionName, String addonKey, String moduleKey)
    {
        AddWorkflowTransitionPostFunctionPage addTransitionPostFunctionPage = goToAddPostFunction();
        addTransitionPostFunctionPage.selectAndSubmitByName(postFunctionName);
        return pageBinder.bind(JiraEditWorkflowTransitionFunctionParamsPage.class, addonKey, moduleKey);
    }
}
