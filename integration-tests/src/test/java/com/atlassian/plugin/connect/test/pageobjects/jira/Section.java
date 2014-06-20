package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;

public class Section
{
    private final String moduleKey;
    private PageElement section;

    @Inject
    private PageElementFinder pageElementFinder;

    public Section(String moduleKey)
    {
        this.moduleKey = moduleKey;
    }

    @Init
    public void init()
    {
        section = pageElementFinder.find(By.id(moduleKey));
    }

    public String getTitle()
    {
        return section.find(By.tagName("h2")).getText();
    }
}
