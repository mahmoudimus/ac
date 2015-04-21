package com.atlassian.plugin.connect.test.pageobjects.jira.workflow;

import com.atlassian.jira.pageobjects.pages.admin.workflow.AddWorkflowTransitionPostFunctionPage;
import com.atlassian.jira.pageobjects.pages.admin.workflow.ViewWorkflowTransitionPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.utils.element.WebDriverPoller;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.webdriver.utils.element.ElementConditions.isVisible;

public class ExtendedViewWorkflowTransitionPage extends ViewWorkflowTransitionPage
{
    private final String workflowMode;
    private final String workflowName;
    private final String workflowStep;
    private final String workflowTransition;

    @Inject
    private PageBinder pageBinder;

    @Inject
    private WebDriverPoller poller;

    public ExtendedViewWorkflowTransitionPage(String workflowMode, String workflowName, String workflowStep, String workflowTransition)
    {
        super(workflowMode, workflowName, workflowStep, workflowTransition);
        this.workflowMode = workflowMode;
        this.workflowName = workflowName;
        this.workflowStep = workflowStep;
        this.workflowTransition = workflowTransition;
    }

    public JiraEditWorkflowTransitionFunctionParamsPage updateFirstPostFunction(String addonKey, String moduleKey)
    {
        poller.waitUntil(isVisible(By.id("view_post_functions")), 20);
        driver.findElement(By.id("view_post_functions")).click();
        poller.waitUntil(isVisible(By.className("criteria-post-function-edit")), 10);
        driver.findElement(By.className("criteria-post-function-edit")).click();
        return pageBinder.bind(JiraEditWorkflowTransitionFunctionParamsPage.class, addonKey, moduleKey);
    }

    public JiraEditWorkflowTransitionFunctionParamsPage goToAddAddonPostFunction(String postFunctionName, String addonKey, String moduleKey)
    {
        AddWorkflowTransitionPostFunctionPage addTransitionPostFunctionPage = goToAddPostFunction();
        addTransitionPostFunctionPage.selectAndSubmitByName(postFunctionName);
        return pageBinder.bind(JiraEditWorkflowTransitionFunctionParamsPage.class, addonKey, moduleKey);
    }
}
