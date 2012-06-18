package com.atlassian.labs.remoteapps.test;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.google.common.collect.Maps.newHashMap;

/**
 *
 */
public class RemoteAppEmbeddedTestPage
{
    @Inject
    private AtlassianWebDriver driver;

    private final String key;
    private WebElement containerDiv;

    public RemoteAppEmbeddedTestPage(String pageKey)
    {
        this.key = pageKey;
    }

    @Init
    public void init()
    {
        this.containerDiv = driver.findElement(By.id("embedded-" + key));
    }

    @WaitUntil
    public void waitForInit()
    {
        driver.waitUntilElementIsLocated(By.className("iframe-init"));
    }

    public String getFullName()
    {
        return waitForValue("user");
    }

    public String getUserId()
    {
        return waitForValue("userId");
    }

    public String getMessage()
    {
        return getValue("message");
    }

    public String getConsumerKey()
    {
        return getValue("consumerKey");
    }

    public String getRestStatus()
    {
        return waitForValue("rest-status");
    }

    public String getRestStatusText()
    {
        return waitForValue("rest-status-text");
    }

    public String getRestContentType()
    {
        return waitForValue("rest-content-type");
    }

    public String getRestResponseText()
    {
        return waitForValue("rest-response-text");
    }

    public String getRestData()
    {
        return waitForValue("rest-data");
    }

    public long getLoadTime()
    {
        return Long.parseLong(driver.findElement(By.id("ra-time-" + key)).getText());
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

    String getValue(final String key)
    {
        return runInFrame(new Callable<String>()
        {

            @Override
            public String call() throws Exception
            {
                return driver.findElement(By.id(key)).getText();
            }
        });
    }

    String waitForValue(final String key)
    {
        runInFrame(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                driver.waitUntil(new Function<WebDriver, Boolean>() {

                    @Override
                    public Boolean apply(WebDriver webDriver) {
                        WebElement element = webDriver.findElement(By.id(key));
                        return element.getText() != null;
                    }
                });
                return null;
            }
        });

        return getValue(key);
    }

    private <T> T runInFrame(Callable<T> runnable)
    {
        final WebElement iframe = containerDiv.findElement(By.tagName("iframe"));
        driver.getDriver().switchTo().frame(iframe);
        T result = null;
        try
        {
            result = runnable.call();
        }
        catch (Exception e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        driver.getDriver().switchTo().defaultContent();
        return result;
    }

    private void toIframe()
    {
        driver.getDriver().switchTo().frame(containerDiv.findElement(By.tagName("iframe")));
    }

    private void outIframe()
    {
        driver.getDriver().switchTo().frame(containerDiv);
    }
}
