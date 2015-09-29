package com.atlassian.plugin.connect.test.pageobjects.confluence;

/**
 * View confluence pages.
 */
public class ConfluenceViewPage extends ConfluenceBasePage
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
