package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.WebDriverElement;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.utils.by.ByJquery;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

@XmlDescriptor(comment="migrate to ConnectPage")
public class RemotePage
{
    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    private PageElementFinder elementFinder;


    protected final String key;
    protected final String extraPrefix;
    protected WebElement containerDiv;

    public RemotePage(String contextKey)
    {
        this(contextKey,"");
    }

    public RemotePage(String contextKey, String extraPrefix)
    {
        this.key = contextKey;
        this.extraPrefix = extraPrefix;
    }

    @WaitUntil
    public void waitForInit()
    {
        PageElement containerDivElement = elementFinder.find(By.id("embedded-" + extraPrefix + key));
        waitUntilTrue(containerDivElement.timed().hasClass("iframe-init"));
        this.containerDiv = ((WebDriverElement)containerDivElement).asWebElement();
    }

    public boolean isLoaded()
    {
        return driver.elementExists(By.cssSelector("#ap-" + extraPrefix + key + " .ap-loading.ap-status.hidden")) &&
                driver.elementExists(By.cssSelector("#ap-" + extraPrefix + key + " .ap-load-timeout.ap-status.hidden")) &&
                driver.elementExists(By.cssSelector("#ap-" + extraPrefix+ key + " .ap-load-error.ap-status.hidden"));
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
        driver.waitUntilElementIsLocated(By.cssSelector("#embedded-" + extraPrefix + key + " iframe"));
        return containerDiv.findElement(By.tagName("iframe"));
    }
}
