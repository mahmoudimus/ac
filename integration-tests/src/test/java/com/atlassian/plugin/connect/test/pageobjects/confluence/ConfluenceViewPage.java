package com.atlassian.plugin.connect.test.pageobjects.confluence;

/**
 * Page for view of confluence pages.
 */
public class ConfluenceViewPage extends ConfluenceBasePage
{
    public ConfluenceViewPage(final String pageId)
    {
        super(pageId);
    }

    @Override
    public String getUrl()
    {
        return "/pages/viewpage.action?pageId=" + getPageId();
    }

}
