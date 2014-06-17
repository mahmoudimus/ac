package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;

public class WorkflowPostFunctionEntry
{
    private final String moduleKey;
    private PageElement entry;

    @Inject
    private PageElementFinder pageElementFinder;

    public WorkflowPostFunctionEntry(String moduleKey)
    {
        this.moduleKey = moduleKey;
    }

    @Init
    public void init()
    {
        entry = pageElementFinder.find(By.id(moduleKey)).find(By.);
    }

    public String getTitle()
    {
        return entry.find(By.tagName("h2")).getText();
    }
}
