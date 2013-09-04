package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static it.TestConstants.IFRAME_ID_SUFFIX;

/**
 * A remote web-panel that is expected to contain some test values.
 */
public class RemoteWebPanel
{
    private static final String IFRAME_ID_PREFIX = "easyXDM_embedded-remote-web-panel-";

    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageElementFinder elementFinder;

    private String id;
    private PageElement iframe;
    private String iframeSrc;

    public RemoteWebPanel(final String id)
    {
        this.id = id;
    }

    @Init
    public void init()
    {
        iframe = elementFinder.find(By.id(IFRAME_ID_PREFIX + id + IFRAME_ID_SUFFIX));
        iframeSrc = iframe.getAttribute("src");

        waitUntilTrue(iframe.timed().isPresent());
    }

    public String getFromQueryString(final String key)
    {
        return RemotePageUtil.findInContext(iframeSrc, key);
    }

    public String getUserId()
    {
        return getFromQueryString("user_id");
    }

    public String getUserKey()
    {
        return getFromQueryString("user_key");
    }

    public String getProjectId()
    {
        return getFromQueryString("project_id");
    }

    public String getIssueId()
    {
        return getFromQueryString("issue_id");
    }

    public String getSpaceId()
    {
        return getFromQueryString("space_id");
    }

    public String getPageId()
    {
        return getFromQueryString("page_id");
    }
}
