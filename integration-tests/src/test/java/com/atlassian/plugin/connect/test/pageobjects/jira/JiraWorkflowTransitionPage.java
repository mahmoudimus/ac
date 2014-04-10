package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.pages.admin.workflow.AddWorkflowTransitionPostFunctionPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.google.common.base.Optional;
import org.openqa.selenium.By;
import com.atlassian.webdriver.utils.element.WebDriverPoller;
import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import com.atlassian.webdriver.utils.element.ElementConditions;
import org.openqa.selenium.WebElement;

/**
 * Created by cwhittington on 10/04/2014.
 */
public class JiraWorkflowTransitionPage implements Page
{
    private String workflowMode;
    private String workflowName;
    private Integer workflowStep;
    private Integer workflowTransition;


    @Inject
    private com.atlassian.webdriver.AtlassianWebDriver driver;

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

    public JiraAddWorkflowTransitionFunctionParamsPage addPostFunction(String addonKey, String postFunctionName)
    {
        poller.waitUntil(ElementConditions.isPresent(By.id("view_post_functions")), 5);
        driver.findElement(By.id("view_post_functions")).click();
        driver.findElement(By.className("criteria-post-function-add")).click();

        // Select post function and submit.
        poller.waitUntil(ElementConditions.isPresent(By.id(addonKey + ":" + postFunctionName)), 5);
        driver.findElement(By.id(addonKey + ":" + postFunctionName)).click();
        driver.findElement(By.id("add_submit")).click();

        return pageBinder.bind(JiraAddWorkflowTransitionFunctionParamsPage.class, postFunctionName);

    }

    public String workflowPostFunctionConfigurationValue(String moduleKey)
    {
        return (String) driver.executeScript("return document.getElementsByClassName('module-" + moduleKey + "')[0].value;");
    }
}
