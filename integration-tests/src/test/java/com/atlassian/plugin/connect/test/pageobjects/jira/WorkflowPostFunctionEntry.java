package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

public class WorkflowPostFunctionEntry
{
    private PageElement entry;

    public WorkflowPostFunctionEntry(PageElement pageElement)
    {
        this.entry = pageElement;

    }

    public String getId()
    {
        return entry.find(By.tagName("input")).getAttribute("id");
    }

    public String getName()
    {
        return entry.findAll(By.tagName("td")).get(1).getText();
    }

    public String getDescription()
    {
        return entry.findAll(By.tagName("td")).get(1).getText();
    }
}
