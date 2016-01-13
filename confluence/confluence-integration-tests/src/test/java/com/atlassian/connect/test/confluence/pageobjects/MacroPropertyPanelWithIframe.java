package com.atlassian.connect.test.confluence.pageobjects;

import com.atlassian.confluence.pageobjects.component.editor.MacroPropertyPanel;
import com.atlassian.pageobjects.elements.PageElement;

import org.openqa.selenium.By;

public class MacroPropertyPanelWithIframe extends MacroPropertyPanel
{
    public boolean hasIframe()
    {
        final PageElement iframe = propertyPanelElement.find(By.tagName("iframe"));

        return iframe.isPresent();
    }

    public long getZIndex() {
        return (Long) propertyPanelElement.javascript().execute("return Number(window.document.defaultView.getComputedStyle(arguments[0]).getPropertyValue('z-index'));");
    }
}
