package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.test.utils.IframeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.Callable;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

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
        if (!elementFinder.find(By.id(dialogId)).timed().isVisible().by(REMOTE_DIALOG_WAIT_MS))
        {
            throw new NoSuchElementException("Couldn't find dialog with id " + dialogId + " in " + REMOTE_DIALOG_WAIT_MS + "ms");
        }
        return pageBinder.bind(RemoteCloseDialogPage.class, dialogId);
    }

    private void open(final String id)
    {
        runInFrame(new Callable<Void>()
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
}
