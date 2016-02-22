package com.atlassian.plugin.connect.test.pageobjects;

import javax.inject.Inject;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonPage;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteCloseDialogPage;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Page with a single button to open a dialog
 */
public class RemoteDialogOpeningPage extends ConnectAddonPage implements Page
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
        return IframeUtils.iframeServletPath(addonKey, pageElementKey);
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
        runInFrame(() -> {
            PageElement element = elementFinder.find(By.id(id));
            waitUntilTrue(element.timed().isVisible());
            element.click();
            return null;
        });
    }
}
