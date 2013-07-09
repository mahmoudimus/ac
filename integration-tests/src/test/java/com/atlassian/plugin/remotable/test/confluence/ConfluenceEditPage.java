package com.atlassian.plugin.remotable.test.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.remotable.test.RemoteWebPanel;

/**
 * Page for edit of confluence pages.
 */
public class ConfluenceEditPage extends RemotePluginEmbeddedTestPage implements Page
{
    private final String pageId;

    public ConfluenceEditPage(final String pageKey, final String pageId)
    {
        super(pageKey);
        this.pageId = pageId;
    }

    @Override
    public String getUrl()
    {
        return "/pages/editpage.action?pageId=" + pageId;
    }

    public RemoteWebPanel getRemoteWebPanel()
    {
        return new RemoteWebPanel(this);
    }
}
