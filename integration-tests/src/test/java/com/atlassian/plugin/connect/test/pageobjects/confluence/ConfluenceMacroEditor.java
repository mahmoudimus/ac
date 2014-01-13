package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.webdriver.AtlassianWebDriver;

import javax.inject.Inject;

public class ConfluenceMacroEditor
{
    @Inject
    AtlassianWebDriver driver;

    @ElementBy(className = "wysiwyg-macro")
    private PageElement wysiwygMacro;

    public void setBody(String text)
    {
        driver.getKeyboard().sendKeys(text);
    }
}
