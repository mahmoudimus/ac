package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.ProductInstance;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.utils.Check;
import com.atlassian.webdriver.utils.element.WebDriverPoller;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.webdriver.utils.element.ElementConditions.isPresent;

public class JiraWorkflowTransitionPage extends AbstractJiraPage {
    private String workflowMode;
    private String workflowName;
    private Integer workflowStep;
    private Integer workflowTransition;


    @Inject
    private com.atlassian.webdriver.AtlassianWebDriver driver;

    @Inject
    ProductInstance productInstance;

    @Inject
    private PageBinder pageBinder;

    @Inject private WebDriverPoller poller;

    public JiraWorkflowTransitionPage(String workflowMode, String workflowName, Integer workflowStep, Integer workflowTransition)
    {
        this.workflowMode = workflowMode;
        this.workflowName = workflowName;
        this.workflowStep = workflowStep;
        this.workflowTransition = workflowTransition;

    }
    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id("workflow-transition-info")).timed().isPresent();
    }

    public JiraWorkflowTransitionPage createOrEditDraft()
    {
        if(Check.elementExists(By.id("create_draft_workflow"), driver))
        {
            driver.findElement(By.id("create_draft_workflow")).click();
        } else {
            driver.findElement(By.id("view_draft_workflow")).click();
        }
        this.workflowMode = "draft";
        this.driver.navigate().to(productInstance.getBaseUrl() + this.getUrl());
        return this;
    }

    @Override
    public String getUrl()
    {
        String url = "";
        try {
            url = "/secure/admin/workflows/ViewWorkflowTransition.jspa?workflowMode=" + workflowMode + "&workflowName=" + URLEncoder.encode(workflowName, "UTF-8") + "&workflowStep=" + workflowStep + "&workflowTransition=" + workflowTransition;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    };


    public JiraAddWorkflowTransitionFunctionParamsPage addPostFunction(String addonKey, String moduleKey)
    {

        poller.waitUntil(isPresent(By.id("view_post_functions")), 5);
        driver.findElement(By.id("view_post_functions")).click();

        poller.waitUntil(isPresent(By.className("criteria-post-function-add")), 5);
        driver.findElement(By.className("criteria-post-function-add")).click();

        // Select post function and submit.
        By radioButton  = By.id("com.atlassian.plugins.atlassian-connect-plugin:" + addonAndModuleKey(addonKey, moduleKey));
        poller.waitUntil(isPresent(radioButton), 5);
        driver.findElement(radioButton).click();
        driver.findElement(By.id("add_submit")).click();

        return pageBinder.bind(JiraAddWorkflowTransitionFunctionParamsPage.class, addonKey, moduleKey);

    }

    public JiraAddWorkflowTransitionFunctionParamsPage updatePostFunction(String addonKey, String moduleKey)
    {
        poller.waitUntil(isPresent(By.id("view_post_functions")), 20);
        driver.findElement(By.id("view_post_functions")).click();
        poller.waitUntil(isPresent(By.className("criteria-post-function-edit")), 5);
        driver.findElement(By.className("criteria-post-function-edit")).click();
        return pageBinder.bind(JiraAddWorkflowTransitionFunctionParamsPage.class, addonKey, moduleKey);

    }

}
