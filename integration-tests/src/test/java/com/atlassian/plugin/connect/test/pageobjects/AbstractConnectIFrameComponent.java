package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.timeout.DefaultTimeouts;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.utils.by.ByJquery;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

public abstract class AbstractConnectIFrameComponent<C>
{
    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    protected PageElementFinder elementFinder;

    protected PageElement iframe;
    protected String iframeSrc;

    protected abstract String getFrameId();

    @Init
    public void init()
    {
        try
        {
            setIFrameAndSrcUnsafe();
        }
        catch (StaleElementReferenceException e)
        {
            // JavaScript code can recreate the iframe while the test is clicking and hovering,
            // and webdriver complains if we are unlucky enough to find the iframe dom element before
            // the re-creation but ask for its attributes after the re-creation
            setIFrameAndSrcUnsafe();
        }

        waitUntilTrue(iframe.timed().isPresent());
    }

    private void setIFrameAndSrcUnsafe()
    {
        iframe = elementFinder.find(By.id(getFrameId()));
        iframeSrc = iframe.getAttribute("src");
    }

    /**
     * Waits until a script tag (any script tag) has loaded. Most iframes containing a script tag pointing at all.js
     * or all-debug.js
     */
    public C waitUntilContentLoaded()
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
        return (C) this;
    }

    public C waitUntilContentElementNotEmpty(final String elementId)
    {
        this.waitUntilContentLoaded();
        // wait until the remote panel has loaded
        waitUntilTrue(Queries.forSupplier(new DefaultTimeouts(), new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return withinIFrame(new Function<WebDriver, Boolean>() {
                    @Override
                    public Boolean apply(WebDriver iframe) {
                        String escapedId = AddonTestUtils.escapeJQuerySelector(elementId);
                        return iframe.findElements(ByJquery.$("#" + escapedId + ":empty")).isEmpty();
                    }
                });
            }
        }));
        return (C) this;
    }

    public String getFromQueryString(final String key)
    {
        return RemotePageUtil.findInContext(iframeSrc, key);
    }

    public String getIFrameSourceUrl()
    {
        return iframeSrc;
    }

    public Dimension getIFrameSize()
    {
        return iframe.getSize();
    }

    public String getIFrameElementText(String elementId)
    {
        return withinIFrame(textOfElement(By.id(elementId)));
    }

    /**
     * Provides a {@link WebDriver} with access to the iframe's content.
     */
    protected <T> T withinIFrame(Function<WebDriver, T> iFrameConsumer)
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

    protected Function<WebDriver, String> textOfElement(final By by)
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
