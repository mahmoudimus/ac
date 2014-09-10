package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.plugin.connect.modules.beans.ReportCategory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.report.ConnectReportModuleDescriptor;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import org.openqa.selenium.By;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * The link to the report on JIRA projects {@link com.atlassian.plugin.connect.test.pageobjects.jira.ProjectCentricNavigationReportLink}.
 */
public class ProjectCentricNavigationReportLink implements ReportLink
{
    private final String title;
    private final String description;
    private final PageElement reportLink;
    private final ReportCategory reportCategory;
    private final String thumbnailCssClass;

    @Inject
    private PageBinder pageBinder;

    public ProjectCentricNavigationReportLink(final PageElement element)
    {
        this.reportLink = element.find(By.tagName("a"));
        this.title = reportLink.getText();
        this.description = element.find(By.tagName("p")).getText();
        this.reportCategory = findReportCategory(element);
        this.thumbnailCssClass = findThumbnailCssClass();
    }

    private ReportCategory findReportCategory(final PageElement element)
    {
        final String reportCategoryStr = element.find(By.xpath("..")).getAttribute("data-category-key");
        return ReportCategory.byKey(reportCategoryStr).get();
    }

    private String findThumbnailCssClass()
    {
        final Iterable<String> cssClasses = Splitter.on(' ').split(reportLink.getAttribute("class"));
        return Iterables.tryFind(cssClasses, new Predicate<String>()
        {
            @Override
            public boolean apply(@Nullable final String cssClass)
            {
                return cssClass != null && cssClass.startsWith(ConnectReportModuleDescriptor.THUMBNAIL_CSS_CLASS_PREFIX);
            }
        }).orNull();
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

    public ReportCategory getReportCategory()
    {
        return reportCategory;
    }

    public String getThumbnailCssClass()
    {
        return thumbnailCssClass;
    }
}
