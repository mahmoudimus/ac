package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;

/**
 * A remote web panel containing a button to call the JavaScript API method jira.refreshIssuePage().
 */
public class RemoteRefreshIssuePageWebPanel extends RemoteWebPanel
{

    public static final String TEMPLATE_PATH = "jira/iframe-refresh-issue-page-button.mu";

    private static final String REFRESH_ISSUE_PAGE_BUTTON_ID = "refresh-issue-page-button";

    @Inject
    private TraceContext traceContext;

    public RemoteRefreshIssuePageWebPanel(String id)
    {
        super(id);
    }

    public RemoteWebPanel waitUntilRefreshIssuePageActionLoaded()
    {
        return waitUntilContentElementNotEmpty(REFRESH_ISSUE_PAGE_BUTTON_ID);
    }

    public Tracer refreshIssuePage()
    {
        Tracer tracer = traceContext.checkpoint();

        withinIFrame(new Function<WebDriver, Void>()
        {

            @Override
            public Void apply(WebDriver driver)
            {
                WebElement button = driver.findElement(By.id(REFRESH_ISSUE_PAGE_BUTTON_ID));
                button.click();
                return null;
            }
        });

        return tracer;
    }
}