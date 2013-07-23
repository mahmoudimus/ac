package com.atlassian.plugin.remotable.test.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.plugin.remotable.test.RemoteWebPanels;

/**
 * Page for edit of confluence pages.
 */
public class ConfluenceEditPage implements Page
{
    private final String pageId;

    @ElementBy (xpath = RemoteWebPanels.REMOTE_WEB_PANELS_XPATH, pageElementClass = RemoteWebPanels.class)
    private RemoteWebPanels webPanels;

    public ConfluenceEditPage(final String pageId)
    {
        this.pageId = pageId;
    }

    @Override
    public String getUrl()
    {
        return "/pages/editpage.action?pageId=" + pageId;
    }

    public RemoteWebPanels getWebPanels()
    {
        return webPanels;
    }
}
