package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.component.header.ConfluenceHeader;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import org.openqa.selenium.By;

import javax.inject.Inject;

public class ConfluenceGeneralPage implements GeneralPage
{

    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder pageBinder;

    private final String pageKey;
    private final String linkText;
    private final String extraPrefix;

    public ConfluenceGeneralPage(String pageKey, String linkText)
    {
        this(pageKey, linkText, "");
    }

    public ConfluenceGeneralPage(String pageKey, String linkText, String extraPrefix)
    {
        this.pageKey = pageKey;
        this.linkText = linkText;
        this.extraPrefix = extraPrefix;
    }

    @Init
    @SuppressWarnings("unused")
    public void init()
    {
        this.pageBinder.bind(ConfluenceHeader.class, new Object[0]);
    }

    @Override
    public boolean isRemotePluginLinkPresent()
    {
        return findLinkElement().isPresent();
    }

    @Override
    public ConnectAddOnEmbeddedTestPage clickAddOnLink()
    {
        findLinkElement().click();
        return pageBinder.bind(ConnectAddOnEmbeddedTestPage.class, extraPrefix, pageKey, true);
    }

    public String getRemotePluginLinkHref()
    {
        return findLinkElement().getAttribute("href");
    }

    private PageElement findLinkElement()
    {
        return elementFinder.find(By.linkText(linkText), TimeoutType.DEFAULT);
    }
}
