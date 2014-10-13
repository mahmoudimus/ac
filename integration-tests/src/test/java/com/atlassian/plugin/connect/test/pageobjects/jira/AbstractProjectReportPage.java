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
 * Abstract page with JIRA project reports.
 */
public abstract class AbstractProjectReportPage implements Page
{
    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder pageBinder;

    protected final String projectKey;
    private final By findReportBy;
    private final By waitFor;
    private final Class<? extends ReportLink> reportLinkClass;

    public AbstractProjectReportPage(final String projectKey, final By findReportBy, final By waitFor, final Class<? extends ReportLink> reportLinkClass)
    {
        this.projectKey = projectKey;
        this.findReportBy = findReportBy;
        this.waitFor = waitFor;
        this.reportLinkClass = reportLinkClass;
    }

    @WaitUntil
    public TimedCondition isOpen()
    {
        return elementFinder.find(waitFor).timed().isPresent();
    }


    public List<ReportLink> getReports()
    {
        List<PageElement> reportLinkElements = elementFinder.findAll(findReportBy);
        return Lists.transform(reportLinkElements, new Function<PageElement, ReportLink>()
        {
            @Override
            public ReportLink apply(final PageElement element)
            {
                return pageBinder.bind(reportLinkClass, element);
            }
        });
    }
}
