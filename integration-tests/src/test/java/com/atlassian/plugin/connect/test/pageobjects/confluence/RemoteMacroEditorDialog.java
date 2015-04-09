package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginDialog;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * A remote macro editor containing a button to call the JavaScript API method confluence.closeMacroEditor().
 */
public class RemoteMacroEditorDialog extends RemotePluginDialog
{

    public static final String TEMPLATE_PATH = "confluence/macro/editor.mu";

    private static final String CLOSE_EDITOR_BUTTON_ID = "macro-editor-close-button";

    public RemoteMacroEditorDialog(ConnectAddOnEmbeddedTestPage embeddedConnectPage)
    {
        super(embeddedConnectPage, false);
    }

    public RemoteMacroEditorDialog closeMacroEditorAndWaitUntilHidden()
    {
        withinIFrame(new Function<WebDriver, Void>()
        {

            @Override
            public Void apply(WebDriver driver)
            {
                PageElement element = elementFinder.find(By.id(CLOSE_EDITOR_BUTTON_ID));
                Poller.waitUntilTrue(element.timed().isVisible());
                element.javascript().mouse().click();
                Poller.waitUntilFalse(element.timed().isPresent());
                return null;
            }
        });

        waitUntilHidden();

        return this;
    }
}
