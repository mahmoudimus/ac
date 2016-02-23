package com.atlassian.connect.test.confluence.pageobjects;

import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.RemotePluginDialog;
import org.openqa.selenium.By;

/**
 * A remote macro editor containing a button to call the JavaScript API method confluence.closeMacroEditor().
 */
public class RemoteMacroEditorDialog extends RemotePluginDialog {

    public static final String TEMPLATE_PATH = "it/confluence/macro/editor.mu";

    private static final String CLOSE_EDITOR_BUTTON_ID = "macro-editor-close-button";

    public RemoteMacroEditorDialog(ConnectAddonEmbeddedTestPage embeddedConnectPage) {
        super(embeddedConnectPage);
    }

    public RemoteMacroEditorDialog closeMacroEditorAndWaitUntilHidden() {
        withinIFrame(driver1 -> {
            PageElement element = elementFinder.find(By.id(CLOSE_EDITOR_BUTTON_ID));
            Poller.waitUntilTrue(element.timed().isVisible());
            element.javascript().mouse().click();
            Poller.waitUntilFalse(element.timed().isPresent());
            return null;
        });

        waitUntilHidden();

        return this;
    }
}
