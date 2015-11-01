package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.webdriver.utils.element.ElementConditions;
import com.atlassian.webdriver.utils.element.WebDriverPoller;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.support.ui.ExpectedCondition;

import javax.inject.Inject;

public abstract class AbstractRemoteDialog<C extends AbstractRemoteDialog> extends AbstractConnectIFrameComponent<C>
{

    @Inject
    protected WebDriverPoller poller;

    protected String getFrameId()
    {
        try
        {
            return getFrameIdUnsafe();
        }
        catch (StaleElementReferenceException e)
        {
            // JavaScript code can recreate the iframe while the test is clicking and hovering,
            // and webdriver complains if we are unlucky enough to find the iframe dom element before
            // the re-creation but ask for its id after the re-creation
            return getFrameIdUnsafe();
        }
    }

    private String getFrameIdUnsafe()
    {
        final String cssClass = getContainerCssClassName();
        return elementFinder.find(By.cssSelector("." + cssClass + " iframe")).getAttribute("id");
    }

    public void waitUntilHidden()
    {
        poller.waitUntil(getHiddenCondition(By.className(getContainerCssClassName())), 10);
    }

    protected ExpectedCondition getHiddenCondition(By locator)
    {
        return ElementConditions.isNotPresent(locator);
    }

    protected abstract String getContainerCssClassName();
}
