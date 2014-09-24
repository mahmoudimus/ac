package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public final class ConfluencePageWithRemoteMacro implements Page
{
    private final String pageTitle;
    private final String macroName;

    public static class MacroTimeoutResult
    {
        final boolean timedOut;
        final String macroText;

        public MacroTimeoutResult(boolean timedOut, String macroText)
        {
            this.timedOut = timedOut;
            this.macroText = macroText;
        }

        public boolean isTimedOut()
        {
            return timedOut;
        }

        public String getMacroText()
        {
            return macroText;
        }
    }

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
        final By locator = By.cssSelector(format(".%s .%s", macroName, className));
        if (driver.elementExists(locator))
        {
            return driver.findElement(locator).getText();
        }
        else
        {
            return null;
        }
    }

    public MacroTimeoutResult macroHasTimedOut()
    {
        if (!hasMacro())
        {
            throw new IllegalStateException("Calling macroHasTimedOut() on a page with no macro makes no sense!");
        }

        driver.waitUntilElementIsNotLocated(By.cssSelector(format(".%s span.bp-loading", macroName)));

        final String text = getMacro().getText();
        return new MacroTimeoutResult(text.contains("java.net.SocketTimeoutException"), text);
    }

    private boolean hasMacro()
    {
        return driver.elementExists(getMacroLocator());
    }

    private WebElement getMacro()
    {
        return driver.findElement(getMacroLocator());
    }

    private By getMacroLocator()
    {
        return By.className(macroName);
    }
}
