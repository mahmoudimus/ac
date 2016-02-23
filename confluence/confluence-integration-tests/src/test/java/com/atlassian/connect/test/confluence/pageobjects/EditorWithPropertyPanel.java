package com.atlassian.connect.test.confluence.pageobjects;

import com.atlassian.confluence.pageobjects.component.editor.EditorContent;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

public class EditorWithPropertyPanel extends EditorContent {
    /**
     * Click on the macro, then wait for the property panel to appear.
     * @param macroName
     * @return The open property panel.
     */
    public MacroPropertyPanelWithIframe openPropertyPanel(final String macroName) {
        execute.onTinyMceIFrame(() -> {
            PageElement inlineMacro = page.find(By.cssSelector(".editor-inline-macro[data-macro-name=\"" + macroName
                    + "\"]"));

            waitUntilTrue(inlineMacro.timed().isVisible());
            inlineMacro.click();

            return null;
        });

        final PageElement propertyPanelElement = page.find(By.id("property-panel"));

        final PageElement iframe = propertyPanelElement.find(By.tagName("iframe"));

        try {
            waitUntilTrue(iframe.timed().isPresent());
        } catch (AssertionError e) {
            //Iframe not found.
        }

        return binder.bind(MacroPropertyPanelWithIframe.class);
    }
}
