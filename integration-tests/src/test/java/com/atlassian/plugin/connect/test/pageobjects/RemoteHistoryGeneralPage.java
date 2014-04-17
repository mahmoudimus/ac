package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.Callable;

/**
 * Page with buttons for executing the javascript history plugin
 */
public class RemoteHistoryGeneralPage extends RemotePage implements Page
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected PageElementFinder elementFinder;

    private final String addonKey;
    private final String moduleKey;


    public RemoteHistoryGeneralPage(String addonKey, String moduleKey) {
        super(moduleKey);
        this.addonKey = addonKey;
        this.moduleKey = moduleKey;
    }

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/ac/"+ addonKey + "/" + moduleKey;
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
                driver.findElement(By.id("forward")).click();
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
        return runInFrame(new Callable<String>()
        {

            @Override
            public String call() throws Exception
            {
                return driver.findElement(By.id("log")).getText();
            }
        });
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