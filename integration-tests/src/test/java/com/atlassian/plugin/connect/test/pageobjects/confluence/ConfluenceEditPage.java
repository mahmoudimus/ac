package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;

import com.google.inject.Inject;

/**
 * Page for edit of confluence pages.
 */
public class ConfluenceEditPage implements Page
{
    private final String pageId;

    @Inject
    private PageBinder pageBinder;

    public ConfluenceEditPage(final String pageId)
    {
        this.pageId = pageId;
    }

    @Override
    public String getUrl()
    {
        return "/pages/editpage.action?pageId=" + pageId;
    }

    public RemoteWebPanel findWebPanel(String id)
    {
        return pageBinder.bind(RemoteWebPanel.class, id);
    }
}
