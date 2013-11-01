package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.timeout.DefaultTimeouts;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

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
        iframe = elementFinder.find(By.id(getFrameId()));
        iframeSrc = iframe.getAttribute("src");

        waitUntilTrue(iframe.timed().isPresent());
    }

    /**
     * Waits until a script tag (any script tag) has loaded. Most web panels containing a script tag pointing at all.js
     * or all-debug.js
     */
    public RemoteWebPanel waitUntilContentLoaded()
    {
        // wait until the remote panel has loaded
        waitUntilTrue(Queries.forSupplier(new DefaultTimeouts(), new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                return withinIFrame(new Function<WebDriver, Boolean>()
                {
                    @Override
                    public Boolean apply(WebDriver iframe)
                    {
                        return !iframe.findElements(By.tagName("script")).isEmpty();
                    }
                });
            }
        }));
        return this;
    }

    private String getFrameId()
    {
        return IFRAME_ID_PREFIX + id + IFRAME_ID_SUFFIX;
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

    public String getIFrameSourceUrl()
    {
        return iframeSrc;
    }

    public PageElement getIFrame()
    {
        return iframe;
    }

    public String getCustomMessage()
    {
        return withinIFrame(textOfElement(By.id("custom-message")));
    }

    public String getApRequestMessage()
    {
        return withinIFrame(textOfElement(By.id("message")));
    }

    public String getApRequestStatusCode()
    {
        return withinIFrame(textOfElement(By.id("client-http-status")));
    }

    public String getApRequestUnauthorizedStatusCode()
    {
        return withinIFrame(textOfElement(By.id("client-http-unauthorized-code")));
    }

    public boolean containsHelloWorld()
    {
        return withinIFrame(new Function<WebDriver, Boolean>()
        {
            @Override
            public Boolean apply(WebDriver frame)
            {
                return frame.findElement(By.id("hello-world-message")).isDisplayed();
            }
        });
    }

    /**
     * Provides a {@link WebDriver} with access to the iframe's content.
     */
    private <T> T withinIFrame(Function<WebDriver, T> iFrameConsumer)
    {
        try
        {
            WebDriver frameDriver = driver.switchTo().frame(getFrameId());
            return iFrameConsumer.apply(frameDriver);
        }
        finally
        {
            driver.switchTo().defaultContent();
        }
    }

    private Function<WebDriver, String> textOfElement(final By by)
    {
        return new Function<WebDriver, String>()
        {
            @Override
            public String apply(WebDriver frame)
            {
                return frame.findElement(by).getText();
            }
        };
    }
}
