package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.jira.pageobjects.framework.util.TimedQueryFactory;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.WebDriverElement;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

public class ConnectAddOnPage
{
    private static final Logger log = LoggerFactory.getLogger(ConnectAddOnPage.class);
    private static final String IFRAME_INIT = "iframe-init";

    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    private PageElementFinder elementFinder;


    protected final String pageElementKey;
    protected final String addOnKey;
    private final  boolean includedEmbeddedPrefix;

    protected WebElement containerDiv;

    public ConnectAddOnPage(String addOnKey, String pageElementKey, boolean includedEmbeddedPrefix)
    {
        this.pageElementKey = pageElementKey;
        this.addOnKey = addOnKey;
        this.includedEmbeddedPrefix = includedEmbeddedPrefix;
    }

    @WaitUntil
    public void waitForInit()
    {
        final String prefix = includedEmbeddedPrefix ? "embedded-" : "";
        final String suffix = StringUtils.isEmpty(addOnKey)
                ? pageElementKey
                : ModuleKeyUtils.addonAndModuleKey(addOnKey, pageElementKey);
        final String id = prefix + suffix;
        PageElement containerDivElement = elementFinder.find(By.id(id), TimeoutType.SLOW_PAGE_LOAD);

        try
        {
            waitUntilTrue(containerDivElement.timed().hasClass(IFRAME_INIT));
        }
        catch (AssertionError e)
        {
            // failed to find the iframe, or iframe initialization never finished...
            // the developer debugging this will appreciate a little help in the bamboo logs
            // so that they don't have to run the product and attach a debugger just to find out the parameters
            log.error("Waiting for the container div '{}' to get the class '{}' timed out. addOnKey='{}', pageElementKey='{}', includeEmbeddedPrefix={}",
                    new Object[]{ id, IFRAME_INIT, addOnKey, pageElementKey, includedEmbeddedPrefix });
            throw e;
        }

        this.containerDiv = ((WebDriverElement)containerDivElement).asWebElement();
    }

    public Map<String, String> getIframeQueryParams()
    {
        return RemotePageUtil.findAllInContext(iframe().getAttribute("src"));
    }

    public String waitForValue(final String key)
    {
        return RemotePageUtil.waitForValue(driver, containerDiv, key);
    }

    public String getValue(final String key)
    {
        return RemotePageUtil.waitForValue(driver, containerDiv, key);
    }

    public <T> T runInFrame(Callable<T> callable)
    {
        return RemotePageUtil.runInFrame(driver, containerDiv, callable);
    }

    public boolean isFullSize()
    {
        return waitForCssClass("full-size-general-page");
    }

    public boolean isNotFullSize()
    {
        // We have to wait for the css class, and waiting for something to NOT appear can take a long time,
        // so we add a failure class here
        return waitForCssClass("full-size-general-page-fail");
    }

    private boolean waitForCssClass(final String cssClass)
    {
        driver.waitUntil(new Function<WebDriver, Boolean>()
        {

            @Override
            public Boolean apply(WebDriver webDriver)
            {
                List<String> classes = Arrays.asList(iframe().getAttribute("class").split(" "));
                return classes.contains(cssClass);
            }
        });
        return true;
    }

    private WebElement iframe()
    {
        return containerDiv.findElement(By.tagName("iframe"));
    }
}
