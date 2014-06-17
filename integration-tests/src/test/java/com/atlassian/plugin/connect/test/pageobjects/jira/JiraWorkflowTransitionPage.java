package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.ProductInstance;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.utils.element.WebDriverPoller;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.webdriver.utils.element.ElementConditions.isPresent;

public class JiraWorkflowTransitionPage extends AbstractJiraPage
{
    private String workflowMode;
    private String workflowName;
    private Integer workflowStep;
    private Integer workflowTransition;

    @ElementBy(id = "descriptors_table")
    private PageElement postSelectionFunctionTable;

    @Inject
    private com.atlassian.webdriver.AtlassianWebDriver driver;

    @Inject
    ProductInstance productInstance;

    @Inject
    private PageBinder pageBinder;

    @Inject
    private WebDriverPoller poller;

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
        return postSelectionFunctionTable.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        String url = "";
        try
        {
            url = "/secure/admin/workflows/AddWorkflowTransitionPostFunction!default.jspa?workflowMode=" + workflowMode + "&workflowName=" + URLEncoder.encode(workflowName, "UTF-8") + "&workflowStep=" + workflowStep + "&workflowTransition=" + workflowTransition;
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return url;
    }

    public JiraAddWorkflowTransitionFunctionParamsPage addPostFunction(String addonKey, String moduleKey)
    {
        // Select post function and submit.
        By radioButton = By.id("com.atlassian.plugins.atlassian-connect-plugin:" + addonAndModuleKey(addonKey, moduleKey));
        poller.waitUntil(isPresent(radioButton), 5);
        driver.findElement(radioButton).click();
        driver.findElement(By.id("add_submit")).click();

        return pageBinder.bind(JiraAddWorkflowTransitionFunctionParamsPage.class, addonKey, moduleKey);
    }

    public JiraAddWorkflowTransitionFunctionParamsPage updatePostFunction(String addonKey, String moduleKey)
    {
        poller.waitUntil(isPresent(By.id("view_post_functions")), 5);
        driver.findElement(By.id("view_post_functions")).click();
        driver.findElement(By.className("criteria-post-function-edit")).click();
        return pageBinder.bind(JiraAddWorkflowTransitionFunctionParamsPage.class, addonKey, moduleKey);
    }

    public List<WorkflowPostFunctionEntry> getPostFunctions()
    {
        List<PageElement> postFunctions = postSelectionFunctionTable.find(By.tagName("tbody")).findAll(By.tagName("tr"));
        return null;
    }

}
