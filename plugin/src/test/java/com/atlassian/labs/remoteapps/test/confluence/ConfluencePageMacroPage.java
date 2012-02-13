package com.atlassian.labs.remoteapps.test.confluence;

import com.atlassian.labs.remoteapps.test.MyIframePage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.AtlassianWebDriver;

import javax.inject.Inject;

/**
 *
 */
public class ConfluencePageMacroPage extends MyIframePage implements Page
{
    @Inject
    AtlassianWebDriver driver;

    @Inject
    PageBinder pageBinder;
    private String title;

    public ConfluencePageMacroPage(String title, String namespace)
    {
        super(namespace);
        this.title = title;
    }

    @Override
    public String getUrl()
    {
        return "/display/ds/" + title;
    }

    
}
