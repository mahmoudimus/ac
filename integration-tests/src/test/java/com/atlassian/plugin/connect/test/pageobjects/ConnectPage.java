package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.WebDriverElement;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.utils.by.ByJquery;
import com.google.common.base.Function;
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

public class ConnectPage
{
    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    private PageElementFinder elementFinder;


    protected final String pageElementKey;
    protected final String addOnKey;
    protected WebElement containerDiv;

    private static final Logger log = LoggerFactory.getLogger(ConnectPage.class);

    public ConnectPage(String pageElementKey, String addOnKey)
    {
        this.pageElementKey = pageElementKey;
        this.addOnKey = addOnKey;
    }

    @WaitUntil
    public void waitForInit()
    {
        final String jquerySelector = "#embedded-" + addOnKey + "__" + pageElementKey;
        PageElement containerDivElement = elementFinder.find(ByJquery.$(jquerySelector));

        try
        {
            waitUntilTrue(containerDivElement.timed().isPresent());
        }
        catch (AssertionError e)
        {
            // log the failed selector so that you don't have to open the debugger to find it
            log.error("Failed to find page element using jquery selector '{}'.", jquerySelector);
            throw e;
        }

        this.containerDiv = ((WebDriverElement)containerDivElement).asWebElement();
    }

    public boolean isLoaded()
    {
        return driver.elementExists(By.cssSelector("#ap-" + addOnKey + pageElementKey + " .ap-loading.ap-status.hidden")) &&
                driver.elementExists(By.cssSelector("#ap-" + addOnKey + pageElementKey + " .ap-load-timeout.ap-status.hidden")) &&
                driver.elementExists(By.cssSelector("#ap-" + addOnKey + pageElementKey + " .ap-load-error.ap-status.hidden"));
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

    public WebElement getContainerDiv()
    {
        return containerDiv;
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
        driver.waitUntilElementIsLocated(By.cssSelector("#embedded-" + addOnKey + pageElementKey + " iframe"));
        return containerDiv.findElement(By.tagName("iframe"));
    }
}
