package com.atlassian.labs.remoteapps.test;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.xml.rpc.Call;
import java.util.concurrent.Callable;

/**
 *
 */
public class MyAdminPage
{
    @Inject
    private AtlassianWebDriver driver;

    @FindBy(id="embedded")
    private WebElement containerDiv;

    @WaitUntil
    public void waitForInit()
    {
        driver.waitUntilElementIsLocated(By.className("iframe-init"));
    }

    public String getMessage()
    {
        return getValue("message");
    }

    public String getConsumerKey()
    {
        return getValue("consumerKey");
    }

    private String getValue(final String key)
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
