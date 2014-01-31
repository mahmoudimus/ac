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
public class RemoteDialogOpeningPage
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected PageElementFinder elementFinder;

    // "servlet" || "remote-web-item"
    private final String type;
    private final String key;
    private final String pluginKey;

    protected WebElement containerDiv;

    public RemoteDialogOpeningPage(String type, String key, String pluginKey)
    {
        this.type = type;
        this.key = key;
        this.pluginKey = pluginKey;
    }

    @Init
    public void init()
    {
        this.containerDiv = driver.findElement(By.id("embedded-" + type + "-" + key));
    }

    public RemoteCloseDialogPage openUrl()
    {
        open("dialog-open-button-url");
        return pageBinder.bind(RemoteCloseDialogPage.class, "ap-" + pluginKey + "-dialog");
    }

    public RemoteCloseDialogPage openKey(String expectedNamespace)
    {
        open("dialog-open-button-key");
        return pageBinder.bind(RemoteCloseDialogPage.class, "ap-" + expectedNamespace);
    }

    private void open(final String id)
    {
        runInFrame(driver, containerDiv, new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                PageElement element = elementFinder.find(By.id(id));
                waitUntilTrue(element.timed().isVisible());

                element = elementFinder.find(By.id(id));
                element.click();
                return null;
            }
        });
    }

    public String waitForValue(String key)
    {
        return RemotePageUtil.waitForValue(driver, containerDiv, key);
    }
}