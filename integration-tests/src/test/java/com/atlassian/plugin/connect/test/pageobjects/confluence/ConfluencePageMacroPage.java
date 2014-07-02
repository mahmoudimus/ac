package com.atlassian.plugin.connect.test.pageobjects.confluence;

import javax.inject.Inject;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

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
