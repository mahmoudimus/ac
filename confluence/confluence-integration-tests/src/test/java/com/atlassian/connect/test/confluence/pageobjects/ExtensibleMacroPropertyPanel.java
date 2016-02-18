package com.atlassian.connect.test.confluence.pageobjects;

import com.atlassian.confluence.pageobjects.component.editor.MacroPropertyPanel;
import com.atlassian.pageobjects.elements.PageElement;

import org.openqa.selenium.By;

import java.util.List;

public class ExtensibleMacroPropertyPanel extends MacroPropertyPanel
{
    public boolean hasButton(String displayName)
    {
        final List<PageElement> buttons = propertyPanelElement.findAll(By.className("panel-button-text"));
        return buttons.stream()
                .map(PageElement::getText)
                .filter(buttonText -> buttonText != null)
                .findFirst()
                .map(foundText -> true)
                .orElse(false);
    }

    public boolean hasIframe()
    {
        final PageElement iframe = propertyPanelElement.find(By.tagName("iframe"));

        return iframe.isPresent();
    }

    public long getZIndex() {
        return (Long) propertyPanelElement.javascript().execute("return Number(window.document.defaultView.getComputedStyle(arguments[0]).getPropertyValue('z-index'));");
    }
}
