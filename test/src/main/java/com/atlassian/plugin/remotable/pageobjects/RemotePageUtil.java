package com.atlassian.plugin.remotable.pageobjects;

import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.concurrent.Callable;

/**
 * Helper methods for retrieving the content from an iframe.
 */
public class RemotePageUtil
{
    public static <T> T runInFrame(AtlassianWebDriver driver, WebElement containerDiv, Callable<T> callable)
    {
        toIframe(driver, containerDiv);
        T result = null;
        try
        {
            result = callable.call();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        outIframe(driver);
        return result;
    }

    public static String waitForValue(final AtlassianWebDriver driver, final WebElement containerDiv, final String key)
    {
        runInFrame(driver, containerDiv, new Callable<Void>()
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

        return getValue(driver, containerDiv, key);
    }

    public static String getValue(final AtlassianWebDriver driver, final WebElement containerDiv, final String key)
    {
        return runInFrame(driver, containerDiv, new Callable<String>()
        {

            @Override
            public String call() throws Exception
            {
                return driver.findElement(By.id(key)).getText();
            }
        });
    }

    public static void toIframe(AtlassianWebDriver driver, WebElement containerDiv)
    {
        driver.getDriver().switchTo().frame(containerDiv.findElement(By.tagName("iframe")));
    }

    public static void outIframe(AtlassianWebDriver driver)
    {
        driver.getDriver().switchTo().defaultContent();
    }
}
