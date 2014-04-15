package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.plugin.module.page.RemotePageDescriptorCreator;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.Callable;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.plugin.connect.test.pageobjects.RemotePageUtil.runInFrame;

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
        runInFrame(new Callable<Void>()
        {

            @Override
            public Void call() throws Exception
            {
                driver.findElement(By.id("back")).click();
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
                driver.findElement(By.id("pushstate")).click();
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