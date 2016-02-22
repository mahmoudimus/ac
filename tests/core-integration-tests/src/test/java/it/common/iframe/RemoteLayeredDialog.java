package it.common.iframe;

import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.RemotePluginDialog;
import org.openqa.selenium.By;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;

/**
 * A dialog that may have other dialogs above or below it.
 */
public class RemoteLayeredDialog extends RemotePluginDialog {
    // If true, this dialog is the lowest in a stack of dialogs.
    private final boolean isBottomDialog;

    public RemoteLayeredDialog(ConnectAddonEmbeddedTestPage embeddedConnectPage, boolean isBottomDialog) {
        super(embeddedConnectPage);
        this.isBottomDialog = isBottomDialog;
    }

    /**
     * Click a custom button in this dialog with the given class.
     *
     * @param className
     */
    public void clickButtonWithClass(String className) {
        elementFinder.find(By.className(className)).click();
    }

    public void waitUntilHidden() {
        waitUntilFalse(iframe.timed().isVisible());
        if (isBottomDialog) {
            waitUntilAUIBlanketHidden();
        }
    }
}
