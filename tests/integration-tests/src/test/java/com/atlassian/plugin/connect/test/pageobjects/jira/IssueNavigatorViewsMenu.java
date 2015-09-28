package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.components.menu.JiraAuiDropdownMenu;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.google.common.base.Preconditions;
import org.openqa.selenium.By;

import javax.inject.Inject;

public class IssueNavigatorViewsMenu
{
    @Inject
    PageBinder pageBinder;

    @Inject
    protected PageElementFinder finder;

    private JiraAuiDropdownMenu viewsMenu;

    @Init
    public void initialise()
    {
        viewsMenu = pageBinder.bind(JiraAuiDropdownMenu.class, By.className("header-views"), By.className("header-views-menu"));
    }

    public IssueNavigatorViewsMenu open()
    {
        viewsMenu.open();
        return this;
    }

    public boolean isOpen()
    {
        return viewsMenu.isOpen();
    }

    public IssueNavigatorViewsMenu close()
    {
        viewsMenu.close();
        return this;
    }

    public ViewEntry entryWithLabel(String label)
    {
        viewsMenu.open();
        PageElement pageElement = finder.find(By.className("header-views-menu")).find(By.linkText(label), TimeoutType.DEFAULT);
        return new ViewEntry(pageElement);
    }


    public static class ViewEntry
    {
        private final PageElement pageElement;

        public ViewEntry(PageElement pageElement)
        {
            Preconditions.checkNotNull(pageElement);
            this.pageElement = pageElement;
        }

        public void click()
        {
            pageElement.click();
        }

        public String getLinkUrl()
        {
            return pageElement.getAttribute("href");
        }

        public String getLinkText()
        {
            return pageElement.getText();
        }

        public boolean isPresent()
        {
            return pageElement.isPresent();
        }
    }
}
