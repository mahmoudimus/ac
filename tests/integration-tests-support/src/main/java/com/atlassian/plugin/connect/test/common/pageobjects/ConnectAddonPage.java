package com.atlassian.plugin.connect.test.common.pageobjects;

import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.WebDriverElement;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

public class ConnectAddonPage {
    private static final Logger log = LoggerFactory.getLogger(ConnectAddonPage.class);
    private static final String IFRAME_INIT = "iframe-init";

    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    private PageElementFinder elementFinder;


    protected final String pageElementKey;
    protected final String addonKey;
    private final boolean includedEmbeddedPrefix;

    private PageElement containerDivElement;
    protected WebElement containerDiv;

    public ConnectAddonPage(String addonKey, String pageElementKey, boolean includedEmbeddedPrefix) {
        this.pageElementKey = pageElementKey;
        this.addonKey = addonKey;
        this.includedEmbeddedPrefix = includedEmbeddedPrefix;
    }

    @WaitUntil
    public void waitForInit() {
        final String prefix = includedEmbeddedPrefix ? "embedded-" : "";
        final String suffix = StringUtils.isEmpty(addonKey)
                ? pageElementKey
                : ModuleKeyUtils.addonAndModuleKey(addonKey, pageElementKey);
        final String id = prefix + suffix;
        containerDivElement = elementFinder.find(By.id(id));
        final long startTime = System.currentTimeMillis();

        try {
            waitUntilTrue(containerDivElement.withTimeout(TimeoutType.PAGE_LOAD).timed().hasClass(IFRAME_INIT));
        } catch (AssertionError e) {
            debugIframeFailure(id, containerDivElement);
            throw e;
        }

        final long stopTime = System.currentTimeMillis();
        log.debug("Milliseconds to find iframe-init class on ap-content container div: {}", stopTime - startTime);
        this.containerDiv = ((WebDriverElement) containerDivElement).asWebElement();

        waitForFirstScriptToLoad();
    }

    private void waitForFirstScriptToLoad() {
        runInFrame(() -> {
            PageElement element = elementFinder.find(By.tagName("script"));
            waitUntilTrue(element.timed().isPresent());
            return null;
        });
    }

    private void debugIframeFailure(String containerDivId, PageElement containerDivElement) {
        // failed to find the iframe, or iframe initialization never finished...
        // the developer debugging this will appreciate a little help in the bamboo logs
        // so that they don't have to run the product and attach a debugger just to find out the parameters
        log.error("Waiting for the container div '{}' to get the class '{}' timed out. addonKey='{}', pageElementKey='{}', includeEmbeddedPrefix={}",
                new Object[]{containerDivId, IFRAME_INIT, addonKey, pageElementKey, includedEmbeddedPrefix});

        // this could be because the add-on is not responding to requests for iframe content, so log iframe content
        try {
            final PageElement iframe = containerDivElement.find(By.tagName("iframe"));

            if (iframe.isPresent()) {
                final String iframeSrc = iframe.getAttribute("src");
                log.debug("iframe src='{}'", iframeSrc);
                log.debug("iframe src response='{}'", IOUtils.toString(URI.create(iframeSrc)));
            } else {
                log.error("iframe element is not present inside div '{}'", containerDivId);
            }
        } catch (Exception e) {
            log.error("Failed to log iframe content for debugging.", e);
        }
    }

    public Map<String, String> getIframeQueryParams() {
        return RemotePageUtil.findAllInContext(iframe().getAttribute("src"));
    }

    // Package-level, intended to only be used from AbstractConnectIFrameComponent subclasses.
    PageElement getIFrame() {
        return containerDivElement.find(By.tagName("iframe"));
    }

    public String waitForValue(final String key) {
        return RemotePageUtil.waitForValue(driver, containerDiv, key);
    }

    public String getValue(final String key) {
        return RemotePageUtil.waitForValue(driver, containerDiv, key);
    }

    public <T> T runInFrame(Callable<T> callable) {
        return RemotePageUtil.runInFrame(driver, containerDiv, callable);
    }

    public boolean isFullSize() {
        return waitForCssClass("full-size-general-page");
    }

    public boolean isNotFullSize() {
        // We have to wait for the css class, and waiting for something to NOT appear can take a long time,
        // so we add a failure class here
        return waitForCssClass("full-size-general-page-fail");
    }

    private boolean waitForCssClass(final String cssClass) {
        driver.waitUntil(webDriver -> {
            List<String> classes = Arrays.asList(iframe().getAttribute("class").split(" "));
            return classes.contains(cssClass);
        });
        return true;
    }

    private WebElement iframe() {
        return containerDiv.findElement(By.tagName("iframe"));
    }
}
