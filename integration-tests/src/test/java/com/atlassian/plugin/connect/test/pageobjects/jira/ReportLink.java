package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import org.openqa.selenium.By;

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

    public ReportLink(final PageElement element)
    {
        PageElement reportTitle = element.find(By.className("version-title"));
        this.title = reportTitle.getText();
        this.reportLink = reportTitle.find(By.tagName("a"));
        this.description = element.find(By.className("version-description")).getText();
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
