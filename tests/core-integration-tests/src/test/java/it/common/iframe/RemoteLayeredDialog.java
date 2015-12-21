package it.common.iframe;

import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.RemotePluginDialog;
import org.openqa.selenium.By;

/**
 * A dialog that may have other dialogs above or below it.
 */
public class RemoteLayeredDialog extends RemotePluginDialog
{
    // If true, this dialog is the lowest in a stack of dialogs.
    private final boolean isBottomDialog;

    public RemoteLayeredDialog(ConnectAddOnEmbeddedTestPage embeddedConnectPage, boolean isBottomDialog)
    {
        super(embeddedConnectPage);
        this.isBottomDialog = isBottomDialog;
    }

    /**
     * Click the first custom button in this dialog.
     */
    public void clickCustomButton()
    {
        elementFinder.find(By.className("ap-dialog-custom-button")).click();
    }

    @Override
    protected void waitUntilAUIBlanketHidden()
    {
        if (isBottomDialog)
        {
            super.waitUntilAUIBlanketHidden();
        }
    }
}
