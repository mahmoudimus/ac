package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.utils.Check;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ConfluencePageWithRemoteMacro implements Page
{
    private final String pageTitle;
    private final String macroName;

    @Inject
    private AtlassianWebDriver driver;

    public ConfluencePageWithRemoteMacro(String title, String macroName)
    {
        this.pageTitle = checkNotNull(title);
        this.macroName = checkNotNull(macroName);
    }

    @Override
    public String getUrl()
    {
        return "/display/ds/" + pageTitle;
    }

    public String getText(String className)
    {
        final By locator = By.className(className);

        if (Check.elementExists(locator, driver))
        {
            return driver.findElement(locator).getText();
        }
        else
        {
            return null;
        }
    }

    public boolean macroHasTimedOut()
    {
        try
        {
            final WebElement container = driver.findElement(By.className("ap-container"));

            if (container.getAttribute("id").startsWith("ap-" + macroName))
            {
                container.findElement(By.className("ap-load-timeout"));
                return true;
            }
        }
        catch (NoSuchElementException e)
        {
            // do nothing
        }

        return false;
    }
}
