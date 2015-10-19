package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.test.utils.IframeUtils;
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
public class RemoteDialogOpeningPage extends ConnectAddOnPage implements Page
{

    private  static final int REMOTE_DIALOG_WAIT_MS = 50000;

    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected PageElementFinder elementFinder;

    public RemoteDialogOpeningPage(String addonKey, String moduleKey) {
        super(addonKey, moduleKey, true);
    }

    @Override
    public String getUrl()
    {
        return IframeUtils.iframeServletPath(addOnKey, pageElementKey);
    }

    public RemoteCloseDialogPage openKey(String expectedNamespace)
    {
        open("dialog-open-button-key");
        String dialogId = "ap-" + expectedNamespace;
        checkDialogVisible(dialogId);
        return pageBinder.bind(RemoteCloseDialogPage.class, dialogId);
    }

    public RemoteCloseDialogPage clickToOpenDialog(String buttonKey, String dialogKey)
    {
        open(buttonKey);
        String dialogId = "ap-" + dialogKey;
        checkDialogVisible(dialogId);
        return pageBinder.bind(RemoteCloseDialogPage.class, dialogId);
    }

    public void open(final String id)
    {
        runInFrame(() -> {
            PageElement element = elementFinder.find(By.id(id));
            waitUntilTrue(element.timed().isVisible());
            element.click();
            return null;
        });
    }

    public void clickButtonByClassName(final String className)
    {
        PageElement element = elementFinder.find(By.className(className));
        waitUntilTrue(element.timed().isVisible());
        element.click();
    }

    public void checkDialogVisible(String dialogId)
    {
        if (!elementFinder.find(By.id(dialogId)).timed().isVisible().by(REMOTE_DIALOG_WAIT_MS))
        {
            throw new NoSuchElementException("Couldn't find dialog with id " + dialogId + " in " + REMOTE_DIALOG_WAIT_MS + "ms");
        }
    }
}
