package com.atlassian.plugin.connect.test.common.pageobjects;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.plugin.connect.test.common.pageobjects.RemotePageUtil.runInFrame;

/**
 * Page with a single button to open a dialog
 */
public class RemoteDialogOpeningPanel extends AbstractConnectIFrameComponent<RemoteDialogOpeningPanel> {

    private static final int REMOTE_DIALOG_WAIT_MS = 50000;

    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected PageElementFinder elementFinder;

    private final String pageElementKey;

    protected WebElement containerDiv;

    public RemoteDialogOpeningPanel(String pageElementKey) {
        this.pageElementKey = pageElementKey;
    }

    @Init
    public void init() {
        this.containerDiv = driver.findElement(By.id("embedded-" + pageElementKey));
    }

    public RemoteCloseDialogPage openKey(String expectedNamespace) {
        open("dialog-open-button-key");
        String dialogId = "ap-" + expectedNamespace;
        if (!elementFinder.find(By.id(dialogId)).timed().isVisible().by(REMOTE_DIALOG_WAIT_MS)) {
            throw new NoSuchElementException("Couldn't find dialog with id " + dialogId + " in " + REMOTE_DIALOG_WAIT_MS + "ms");
        }
        return pageBinder.bind(RemoteCloseDialogPage.class, dialogId);
    }

    private void open(final String id) {
        runInFrame(driver, containerDiv, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PageElement element = elementFinder.find(By.id(id));
                waitUntilTrue(element.timed().isVisible());
                element.click();
                return null;
            }
        });
    }

    public String waitForValue(String key) {
        return RemotePageUtil.waitForValue(driver, containerDiv, key);
    }

    @Override
    protected String getFrameId() {
        return IframeUtils.iframeId("embedded-" + pageElementKey);
    }
}