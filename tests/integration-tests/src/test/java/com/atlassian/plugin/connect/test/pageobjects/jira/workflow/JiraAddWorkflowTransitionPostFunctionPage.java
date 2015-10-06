package com.atlassian.plugin.connect.test.pageobjects.jira.workflow;

import com.atlassian.jira.pageobjects.pages.admin.workflow.AddWorkflowTransitionPostFunctionPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import javax.annotation.Nullable;
import java.util.List;

public class JiraAddWorkflowTransitionPostFunctionPage extends AddWorkflowTransitionPostFunctionPage
{
    @ElementBy(id = "descriptors_table")
    private PageElement descriptorsTable;

    public JiraAddWorkflowTransitionPostFunctionPage(String workflowMode, String workflowName, String stepNumber, String transitionNumber)
    {
        super(workflowMode, workflowName, stepNumber, transitionNumber);
    }

    public List<WorkflowPostFunctionEntry> getPostFunctions()
    {
        List<PageElement> postFunctions = descriptorsTable.find(By.tagName("tbody")).findAll(By.tagName("tr"));
        return Lists.transform(postFunctions, new Function<PageElement, WorkflowPostFunctionEntry>()
        {
            @Override
            public WorkflowPostFunctionEntry apply(@Nullable PageElement pageElement)
            {
                return new WorkflowPostFunctionEntry(pageElement);
            }
        });
    }
}
