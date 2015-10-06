package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.Page;

/**
 * View confluence pages.
 */
public class ConfluenceViewPage implements Page
{
    private final String pageId;

    public ConfluenceViewPage(final String pageId)
    {
        this.pageId = pageId;
    }

    @Override
    public String getUrl()
    {
        return "/pages/viewpage.action?pageId=" + pageId;
    }

    public String getPageId()
    {
        return pageId;
    }
}
