package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.Callable;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.plugin.connect.test.pageobjects.RemotePageUtil.runInFrame;

/**
 * Page with a single button to open a dialog
 */
public class RemoteDialogOpeningPage
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private  static final int REMOTE_DIALOG_WAIT_MS = 20000;

    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected PageElementFinder elementFinder;

    // "servlet" || "remote-web-item"
    @XmlDescriptor
    @Deprecated
    private final String type;

    private final String pageElementKey;
    private final String pluginKey;

    @XmlDescriptor
    private final boolean isXmlDescriptor;

    protected WebElement containerDiv;

    @XmlDescriptor
    public RemoteDialogOpeningPage(String type, String pageElementKey, String pluginKey)
    {
        this(type, pageElementKey, pluginKey, true);
    }

    public RemoteDialogOpeningPage(String type, String pageElementKey, String pluginKey, boolean isXmlDescriptor)
    {
        this.type = type;
        this.pageElementKey = pageElementKey;
        this.pluginKey = pluginKey;
        this.isXmlDescriptor = isXmlDescriptor;
    }

    @Init
    public void init()
    {
        this.containerDiv = driver.findElement(By.id("embedded-" + ( (type == null) ? "" : (type + "-") ) + pageElementKey));
    }

    public RemoteCloseDialogPage openKey(String expectedNamespace)
    {
        open("dialog-open-button-key");
        String dialogId = "ap-" + expectedNamespace;
        if (!elementFinder.find(By.id(dialogId)).timed().isVisible().by(REMOTE_DIALOG_WAIT_MS))
        {
            throw new NoSuchElementException("Couldn't find dialog with id " + dialogId + " in " + REMOTE_DIALOG_WAIT_MS + "ms");
        }
        return pageBinder.bind(RemoteCloseDialogPage.class, dialogId);
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