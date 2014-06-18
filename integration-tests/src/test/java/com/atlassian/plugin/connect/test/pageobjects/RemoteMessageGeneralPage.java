package com.atlassian.plugin.connect.test.pageobjects;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.plugin.connect.test.pageobjects.RemotePageUtil.runInFrame;

/**
 * Page with a single button to open a dialog
 */
public class RemoteMessageGeneralPage
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

    public RemoteMessageGeneralPage(String addonAndModuleKey)
    {
        this.addonAndModuleKey = addonAndModuleKey;
    }


    @Init
    public void init()
    {
        this.containerDiv = driver.findElement(By.id("embedded-" + this.addonAndModuleKey));
    }



    public void openInfoMessage()
    {
        runInFrame(driver, containerDiv, new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                PageElement element = elementFinder.find(By.id("display-message"));
                waitUntilTrue(element.timed().isVisible());
                element.click();
                return null;
            }
        });

    }

    public String getMessageTitleText()
    {
        return elementFinder.find(By.cssSelector("#ac-message-container .aui-message .title")).getText();
    }


}