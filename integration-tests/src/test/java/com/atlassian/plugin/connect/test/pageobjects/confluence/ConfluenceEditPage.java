package com.atlassian.plugin.connect.test.pageobjects.confluence;

/**
 * Page for edit of confluence pages.
 */
public class ConfluenceEditPage extends ConfluenceBasePage
{
    public ConfluenceEditPage(final String pageId)
    {
        super(pageId);
    }

    @Override
    public String getUrl()
    {
        return "/pages/editpage.action?pageId=" + getPageId();
    }
}
