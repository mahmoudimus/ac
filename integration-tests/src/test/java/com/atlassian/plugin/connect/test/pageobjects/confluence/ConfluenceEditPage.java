package com.atlassian.plugin.connect.test.pageobjects.confluence;

/**
 * Edit Confluence pages.
 */
public class ConfluenceEditPage extends ConfluenceBasePage
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
