package com.atlassian.plugin.connect.test.confluence.pageobjects;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.common.pageobjects.RemotePageUtil;
import org.openqa.selenium.By;

import javax.inject.Inject;

public class ConfluenceGeneralPage implements GeneralPage
{

    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder pageBinder;

    private final String pageKey;
    private final String addonKey;

    public ConfluenceGeneralPage(String pageKey, String addonKey)
    {
        this.pageKey = pageKey;
        this.addonKey = addonKey;
    }

    @Init
    @SuppressWarnings("unused")
    public void init()
    {
        Poller.waitUntilTrue(findLinkElement().withTimeout(TimeoutType.PAGE_LOAD).timed().isVisible());
    }

    @Override
    public ConnectAddOnEmbeddedTestPage clickAddOnLink()
    {
        clickAddOnLinkWithoutBinding();
        return pageBinder.bind(ConnectAddOnEmbeddedTestPage.class, addonKey, pageKey, true);
    }

    @Override
    public void clickAddOnLinkWithoutBinding()
    {
        PageElement link = findLinkElement();
        RemotePageUtil.clickAddonLinkWithKeyboardFallback(link);
    }

    public String getRemotePluginLinkHref()
    {
        return findLinkElement().getAttribute("href");
    }

    private PageElement findLinkElement()
    {
        return elementFinder.find(By.id(ModuleKeyUtils.addonAndModuleKey(addonKey, pageKey)), TimeoutType.DEFAULT);
    }
}
