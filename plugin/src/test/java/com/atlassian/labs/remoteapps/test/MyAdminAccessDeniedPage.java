package com.atlassian.labs.remoteapps.test;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.inject.Inject;
import java.util.concurrent.Callable;

/**
 *
 */
public class MyAdminAccessDeniedPage implements Page
{
    @Inject
    private AtlassianWebDriver driver;

    @FindBy(id="errorMessage")
    private WebElement errorMessageDiv;

    public String getMessage()
    {
        return errorMessageDiv.getText();
    }

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/remoteapps/app1/remoteAppAdmin";
    }
}
