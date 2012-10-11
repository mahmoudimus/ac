package com.atlassian.plugin.remotable.test.confluence;

import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.AtlassianWebDriver;

import javax.inject.Inject;

/**
 *
 */
public class ConfluencePageMacroPage extends RemotePluginEmbeddedTestPage implements Page
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
