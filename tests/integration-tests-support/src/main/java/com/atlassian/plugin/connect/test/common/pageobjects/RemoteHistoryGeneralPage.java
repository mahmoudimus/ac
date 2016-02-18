package com.atlassian.plugin.connect.test.common.pageobjects;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;
import com.atlassian.webdriver.AtlassianWebDriver;

import com.google.common.base.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page with buttons for executing the javascript history plugin
 */
public class RemoteHistoryGeneralPage extends ConnectAddonPage implements Page
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    protected AtlassianWebDriver driver;

    public RemoteHistoryGeneralPage(String addonKey, String moduleKey) {
        super(addonKey, moduleKey, true);
    }

    @Override
    public String getUrl()
    {
        return IframeUtils.iframeServletPath(addonKey, pageElementKey);
    }

    public String hostUrl() {
        return driver.getCurrentUrl();
    }

    public void browserForward() {
        driver.navigate().forward();
    }

    public void browserBack() {
        driver.navigate().back();
    }

    private boolean waitForHistoryChange(final String oldText)
    {

        driver.waitUntil(new Function<WebDriver, Boolean>()
        {

            @Override
            public Boolean apply(WebDriver webDriver)
            {
                return !driver.findElement(By.id("log")).getText().equals(oldText);

            }
        });
        return true;
    }

    public void javascriptForward() {
        runInFrame(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                String text = driver.findElement(By.id("log")).getText();
                driver.findElement(By.id("forward")).click();
                waitForHistoryChange(text);
                return null;
            }
        });
    }

    public void javascriptBack() {
        runInFrame(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                String text = driver.findElement(By.id("log")).getText();
                driver.findElement(By.id("back")).click();
                waitForHistoryChange(text);
                return null;
            }
        });
    }

    public void javascriptPushState() {
        runInFrame(new Callable<Void>()
        {

            @Override
            public Void call() throws Exception
            {
                String text = driver.findElement(By.id("log")).getText();
                driver.findElement(By.id("pushstate")).click();
                waitForHistoryChange(text);
                return null;
            }
        });
    }

    public String logMessage(){
        return getValue("log");
    }

    public void clearLog(){
        runInFrame(new Callable<String>()
        {

            @Override
            public String call() throws Exception
            {
                driver.findElement(By.id("clearlog")).click();
                return null;
            }
        });
    }

    public String logNewUrl(){
        return runInFrame(new Callable<String>()
        {

            @Override
            public String call() throws Exception
            {
                return driver.findElement(By.className("newurl")).getText();
            }
        });
    }
    public String logOldUrl(){
        return runInFrame(new Callable<String>()
        {

            @Override
            public String call() throws Exception
            {
                return driver.findElement(By.className("oldurl")).getText();
            }
        });
    }
}
