package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.Page;

/**
 * Edit Confluence pages.
 */
public class ConfluenceEditPage implements Page
{
    private final String pageId;

    public ConfluenceEditPage(final String pageId)
    {
        this.pageId = pageId;
    }

    @Override
    public String getUrl()
    {
        return "/pages/editpage.action?pageId=" + pageId;
    }

    public String getPageId()
    {
        return pageId;
    }
}
