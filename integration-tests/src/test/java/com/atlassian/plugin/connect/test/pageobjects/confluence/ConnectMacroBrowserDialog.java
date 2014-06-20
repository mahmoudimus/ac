package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.atlassian.confluence.pageobjects.component.dialog.*;

public class ConnectMacroBrowserDialog extends MacroBrowserDialog {

    @ElementBy(className = "ok")
    private PageElement okButton;

    public void clickSave()
    {
        driver.waitUntil(new Function<WebDriver, Boolean>()
        {

            @Override
            public Boolean apply(WebDriver webDriver)
            {
                if(okButton.isVisible()){
                    okButton.click();
                }
                return !getDialog().isVisible();
            }
        });

    }

}
