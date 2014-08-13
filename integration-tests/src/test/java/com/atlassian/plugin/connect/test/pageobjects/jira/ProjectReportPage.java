package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import java.util.List;
import javax.inject.Inject;

/**
 * Page with JIRA project reports.
 */
public class ProjectReportPage implements Page
{
    private final String projectKey;

    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder pageBinder;

    public ProjectReportPage(final String projectKey)
    {
        this.projectKey = projectKey;
    }

    @WaitUntil
    public TimedCondition isOpen()
    {
        return elementFinder.find(By.id("project-tab")).timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return String.format("/browse/%s/?selectedTab=com.atlassian.jira.jira-projects-plugin:reports-panel", projectKey);
    }

    public List<ReportLink> getReports()
    {
        List<PageElement> reportLinkElements = elementFinder.findAll(By.className("version-block-container"));
        return Lists.transform(reportLinkElements, new Function<PageElement, ReportLink>()
        {
            @Override
            public ReportLink apply(final PageElement element)
            {
                return pageBinder.bind(ReportLink.class, element);
            }
        });
    }
}
