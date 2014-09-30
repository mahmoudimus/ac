package com.atlassian.plugin.connect.test.pageobjects;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.plugin.connect.test.pageobjects.RemotePageUtil.runInFrame;

/**
 * Page with a single button to open a dialog
 */
public class RemoteCookieGeneralPage
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected PageElementFinder elementFinder;

    private final String addonAndModuleKey;

    protected WebElement containerDiv;

    public RemoteCookieGeneralPage(String addonAndModuleKey)
    {
        this.addonAndModuleKey = addonAndModuleKey;
    }


    @Init
    public void init()
    {
        this.containerDiv = driver.findElement(By.id("embedded-" + this.addonAndModuleKey));
    }



    public void saveCookie()
    {
        runInFrame(driver, containerDiv, new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                PageElement element = elementFinder.find(By.id("save-cookie"));
                waitUntilTrue(element.timed().isVisible());
                element.click();
                return null;
            }
        });

    }

    public void readCookie()
    {
        runInFrame(driver, containerDiv, new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                PageElement element = elementFinder.find(By.id("read-cookie"));
                waitUntilTrue(element.timed().isVisible());
                element.click();
                return null;
            }
        });

    }

    public void eraseCookie()
    {
        runInFrame(driver, containerDiv, new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                PageElement element = elementFinder.find(By.id("erase-cookie"));
                waitUntilTrue(element.timed().isVisible());
                element.click();
                return null;
            }
        });

    }

    public String getCookieContents()
    {
        return runInFrame(driver, containerDiv, new Callable<String>()
        {

            @Override
            public String call() throws Exception
            {
                return driver.findElement(By.id("cookie-contents")).getText();
            }
        });
    }

}
