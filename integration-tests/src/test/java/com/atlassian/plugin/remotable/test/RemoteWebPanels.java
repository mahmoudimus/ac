package com.atlassian.plugin.remotable.test;

import com.atlassian.pageobjects.elements.WebDriverElement;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.plugin.remotable.plugin.module.webpanel.RemoteWebPanelModuleDescriptor.REMOTE_WEB_PANEL_MODULE_PREFIX;

/**
 * Set of remote web panels on a particular page.
 */
public class RemoteWebPanels extends WebDriverElement
{
    public static final String REMOTE_WEB_PANELS_XPATH = "//div[contains(@id, 'embedded-remote-web-panel')]";

    @Inject
    AtlassianWebDriver driver;

    public RemoteWebPanels(By locator, TimeoutType timeoutType)
    {
        super(locator, timeoutType);
    }

    public RemoteWebPanel getWebPanel(String panelId)
    {
        return find(By.id(REMOTE_WEB_PANEL_MODULE_PREFIX + panelId), RemoteWebPanel.class);
    }

}
