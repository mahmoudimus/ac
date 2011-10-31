package com.atlassian.labs.remoteapps.test;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.inject.Inject;
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
        driver.waitUntilElementIsLocatedAt(By.tagName("iframe"), containerDiv);
    }

    public String getMessage()
    {
        return getValue("message");
    }

    public String getConsumerKey()
    {
        return getValue("consumerKey");
    }

    public String getRemoteUsername()
    {
        return getValue("remoteUser");
    }

    public String getForbiddenApiStatusCode()
    {
        return getValue("forbiddenGet");
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
