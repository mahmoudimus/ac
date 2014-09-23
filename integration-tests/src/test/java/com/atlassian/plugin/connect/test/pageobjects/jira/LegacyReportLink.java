package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.plugin.connect.modules.beans.ReportCategory;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * The link to the report on {@link com.atlassian.plugin.connect.test.pageobjects.jira.LegacyProjectReportPage}.
 */
public class LegacyReportLink implements ReportLink
{
    private final String title;
    private final String description;
    private final PageElement reportLink;

    @Inject
    private PageBinder pageBinder;

    public LegacyReportLink(final PageElement element)
    {
        PageElement reportTitle = element.find(By.className("version-title"));
        this.title = reportTitle.getText();
        this.reportLink = reportTitle.find(By.tagName("a"));
        this.description = element.find(By.className("version-description")).getText();
    }

    @Init
    @Override
    public void init()
    {
        waitUntilTrue(reportLink.timed().isPresent());
    }

    @Override
    public ConnectAddOnEmbeddedTestPage open(final String pluginKey, final String moduleKey)
    {
        reportLink.click();
        return pageBinder.bind(ConnectAddOnEmbeddedTestPage.class, pluginKey, moduleKey, true);
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public ReportCategory getReportCategory()
    {
        return null;
    }

    @Override
    public String getThumbnailCssClass()
    {
        return null;
    }
}
