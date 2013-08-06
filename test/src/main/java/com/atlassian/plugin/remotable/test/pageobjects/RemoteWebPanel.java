package com.atlassian.plugin.remotable.test.pageobjects;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.net.URI;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * A remote web-panel that is expected to contain some test values.
 */
public class RemoteWebPanel
{
    private static final String IFRAME_ID_PREFIX = "easyXDM_embedded-remote-web-panel-";
    private static final String IFRAME_ID_SUFFIX = "_provider";

    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageElementFinder elementFinder;

    private String id;
    private PageElement iframe;

    public RemoteWebPanel(final String id)
    {
        this.id = id;
    }

    @Init
    public void init()
    {
        iframe = elementFinder.find(By.id(IFRAME_ID_PREFIX + id + IFRAME_ID_SUFFIX));
        waitUntilTrue(iframe.timed().isPresent());
    }

    public String getFromQueryString(String key)
    {
        String src = iframe.getAttribute("src");
        for (NameValuePair pair : URLEncodedUtils.parse(URI.create(src), "UTF-8"))
        {
            if (key.equals(pair.getName()))
            {
                return pair.getValue();
            }
        }
        return null;
    }

    public String getUserId()
    {
        return getFromQueryString("user_id");
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
