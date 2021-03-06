package com.atlassian.plugin.connect.test.common.pageobjects;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.timeout.DefaultTimeouts;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.utils.by.ByJquery;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

public abstract class AbstractConnectIFrameComponent<C extends AbstractConnectIFrameComponent> {
    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    protected PageElementFinder elementFinder;

    protected PageElement iframe;
    protected String iframeSrc;

    protected abstract String getFrameId();

    protected AbstractConnectIFrameComponent() {
    }

    protected AbstractConnectIFrameComponent(PageElement iframe) {
        this.iframe = iframe;
    }

    @Init
    public void init() {
        setIFrameAndSrc();
        waitUntilTrue(iframe.timed().isPresent());
    }

    private void setIFrameAndSrc() {
        try {
            setIFrameAndSrcUnsafe();
        } catch (StaleElementReferenceException e) {
            // JavaScript code can recreate the iframe while the test is clicking and hovering,
            // and webdriver complains if we are unlucky enough to find the iframe dom element before
            // the re-creation but ask for its attributes after the re-creation
            setIFrameAndSrcUnsafe();
        }
    }

    private void setIFrameAndSrcUnsafe() {
        // A constructor variant allows the iframe element to be passed in, in which case we don't need to find it again.
        if (iframe == null) {
            iframe = elementFinder.find(By.id(getFrameId()));
        }
        if (iframeSrc == null) {
            iframeSrc = iframe.getAttribute("src");
        }
    }

    /**
     * Waits until a script tag (any script tag) has loaded. Most iframes containing a script tag pointing at all.js
     * or all-debug.js
     */
    @SuppressWarnings("unchecked")
    public C waitUntilContentLoaded() {
        // wait until the remote panel has loaded
        waitUntilTrue(Queries.forSupplier(new DefaultTimeouts(), () -> withinIFrame(iframe -> !iframe.findElements(By.tagName("script")).isEmpty())));
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    public C waitUntilContentElementNotEmpty(final String elementId) {
        this.waitUntilContentLoaded();

        // wait until the remote panel has loaded
        waitUntilTrue(Queries.forSupplier(new DefaultTimeouts(), () -> withinIFrame(iframe -> {
            String escapedId = AddonTestUtils.escapeJQuerySelector(elementId);
            return iframe.findElements(ByJquery.$("#" + escapedId + ":empty")).isEmpty();
        })));

        return (C) this;
    }

    public String getFromQueryString(final String key) {
        return RemotePageUtil.findInContext(iframeSrc, key);
    }

    public String getIFrameSourceUrl() {
        return iframeSrc;
    }

    public Dimension getIFrameSize() {
        return iframe.getSize();
    }

    public String getIFrameElementText(String elementId) {
        waitUntilContentElementNotEmpty(elementId);
        return withinIFrame(textOfElement(By.id(elementId)));
    }

    public String getIFrameElement(String elementId) {
        waitUntilContentElementNotEmpty(elementId);
        return withinIFrame(htmlOfElement(By.id(elementId)));
    }

    /**
     * Provides a {@link WebDriver} with access to the iframe's content.
     */
    protected <T> T withinIFrame(Function<WebDriver, T> iFrameConsumer) {
        setIFrameAndSrc();

        try {
            WebDriver frameDriver = driver.switchTo().frame(iframe.getAttribute("id"));
            return iFrameConsumer.apply(frameDriver);
        } finally {
            driver.switchTo().defaultContent();
        }
    }

    protected Function<WebDriver, String> textOfElement(final By by) {
        return frame -> frame.findElement(by).getText();
    }

    protected Function<WebDriver, String> htmlOfElement(final By by) {
        return frame -> frame.findElement(by).getAttribute("innerHTML");
    }
}
