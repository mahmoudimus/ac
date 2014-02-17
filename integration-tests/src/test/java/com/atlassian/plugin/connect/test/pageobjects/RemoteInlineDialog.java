package com.atlassian.plugin.connect.test.pageobjects;

import javax.inject.Inject;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes a <webitem> inline dialog - must be bound after the inline dialog has been opened.
 */
public class RemoteInlineDialog extends AbstractConnectIFrameComponent<RemoteInlineDialog>
{
    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    protected PageBinder pageBinder;

    protected String getFrameId()
    {
        return elementFinder.find(By.cssSelector(".aui-inline-dialog iframe")).getAttribute("id");
    }

}