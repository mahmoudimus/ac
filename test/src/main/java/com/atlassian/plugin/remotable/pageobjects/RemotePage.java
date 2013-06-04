package com.atlassian.plugin.remotable.pageobjects;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.google.common.collect.Maps.newHashMap;

/**
 *
 */
public class RemotePage
{
    @Inject
    protected AtlassianWebDriver driver;

    protected final String key;
    protected WebElement containerDiv;

    public RemotePage(String contextKey)
    {
        this.key = contextKey;
    }

    @WaitUntil
    public void waitForInit()
    {
        driver.waitUntilElementIsLocated(By.id("embedded-" + key));
        this.containerDiv = driver.findElement(By.id("embedded-" + key));
        driver.waitUntil(new Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(@Nullable WebDriver input) {
                return containerDiv.getAttribute("class").contains("iframe-init");
            }
        });
    }

    public boolean isLoaded()
    {
        return driver.elementExists(By.cssSelector("#ap-" + key + " .ap-loaded"));
    }

    public Map<String,String> getIframeQueryParams()
    {
        final WebElement iframe = containerDiv.findElement(By.tagName("iframe"));
        String iframeSrc = iframe.getAttribute("src");
        Map<String,String> result = newHashMap();
        for (NameValuePair pair : URLEncodedUtils.parse(URI.create(iframeSrc), "UTF-8"))
        {
            result.put(pair.getName(), pair.getValue());
        }
        return result;
    }

    public <T> T runInFrame(Callable<T> runnable)
    {
        toIframe();
        T result = null;
        try
        {
            result = runnable.call();
        }
        catch (Exception e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        outIframe();
        return result;
    }

    public void toIframe()
    {
        driver.getDriver().switchTo().frame(containerDiv.findElement(By.tagName("iframe")));
    }

    public void outIframe()
    {
        driver.getDriver().switchTo().defaultContent();
    }

    public WebElement getContainerDiv()
    {
        return containerDiv;
    }
}
