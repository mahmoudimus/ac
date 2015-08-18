package it.common.upm;


import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.upm.pageobjects.InstalledPlugin;
import com.atlassian.upm.pageobjects.InstalledPluginDetails;
import com.atlassian.upm.pageobjects.Link;
import com.atlassian.upm.pageobjects.WebElements;
import org.openqa.selenium.By;

import java.net.URI;



public class InstalledPluginDetailsTemporaryExtension extends InstalledPluginDetails
{
    private final PageElement pluginDetails;
    
    public InstalledPluginDetailsTemporaryExtension(PageElement pageElement, InstalledPlugin installedPlugin)
    {
        super(pageElement, installedPlugin);
        this.pluginDetails = pageElement;
        
    }
    public Link getPostInstallLink()
    {
        return new Link(URI.create(getButton(PluginAction.GET_STARTED).getAttribute("href")));
    }

    private PageElement getButton(PluginAction a)
    {
        return getActionButton(pluginDetails, a);
    }
    
    private PageElement getActionButton(PageElement container, PluginAction action)
    {
        return container.find(By.cssSelector("a[data-action=\"" + action.name() + "\"]"));
    }
}
