package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.test.pageobjects.AbstractConnectIFrameComponent;
import com.atlassian.plugin.connect.test.utils.IframeUtils;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * A remote macro editor containing a button to call the JavaScript API method confluence.closeMacroEditor().
 */
public class RemoteMacroEditor extends AbstractConnectIFrameComponent<RemoteMacroEditor>
{

    public static final String TEMPLATE_PATH = "confluence/macro/editor.mu";

    private static final String CLOSE_EDITOR_BUTTON_ID = "macro-editor-close-button";

    private final String id;

    public RemoteMacroEditor(String id)
    {
        this.id = id;
    }

    protected String getFrameId()
    {
        return IframeUtils.iframeId(id);
    }


    public RemoteMacroEditor closeMacroEditor()
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

        return this;
    }
}
