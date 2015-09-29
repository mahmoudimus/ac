package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.test.utils.IframeUtils;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.Callable;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.plugin.connect.test.pageobjects.RemotePageUtil.runInFrame;

/**
 * Page with a single button to open a dialog
 */
public class RemoteCloseDialogPage extends AbstractConnectIFrameComponent<RemoteCloseDialogPage>
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected PageElementFinder elementFinder;

    private String key;

    protected PageElement containerDiv;
    protected PageElement iframe;

    public RemoteCloseDialogPage(String key)
    {
        this.key = key;
    }

    @Init
    public void init()
    {
        this.containerDiv = elementFinder.find(By.id(key));
        waitUntilTrue(this.containerDiv.timed().isPresent());
        this.iframe = containerDiv.find(By.tagName("iframe"));
        waitUntilTrue(this.iframe.timed().isPresent());
    }

    public RemoteCloseDialogPage close()
    {
        WebElement containerDiv = driver.findElement(By.id(key)); // have to repeat as we can't get this from PageElement
        runInFrame(driver, containerDiv, new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                PageElement element = elementFinder.find(By.id("dialog-close-button"));
                waitUntilTrue(element.timed().isVisible());
                element.javascript().mouse().click();
                return null;
            }
        });
        return this;
    }

    public void waitUntilClosed()
    {
        waitUntilFalse(this.containerDiv.timed().isPresent());
    }

    public String getFromQueryString(final String key)
    {
        return RemotePageUtil.findInContext(iframe.getAttribute("src"), key);
    }

    public Dimension getIFrameSize()
    {
        return iframe.getSize();
    }

    @Override
    protected String getFrameId()
    {
        return IframeUtils.iframeId(key);
    }

}