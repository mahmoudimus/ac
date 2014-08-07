package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * The link to the report on JIRA projects report page.
 */
public class ReportLink
{
    private final String title;
    private final String description;
    private final PageElement reportLink;

    @Inject
    private PageBinder pageBinder;

    public ReportLink(final String title, final String description, final PageElement reportLink)
    {
        this.title = title;
        this.description = description;
        this.reportLink = reportLink;
    }

    @Init
    public void init()
    {
        waitUntilTrue(reportLink.timed().isPresent());
    }

    public ConnectAddOnEmbeddedTestPage open(final String pluginKey, final String moduleKey)
    {
        reportLink.click();
        return pageBinder.bind(ConnectAddOnEmbeddedTestPage.class, pluginKey, moduleKey, true);
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }
}
