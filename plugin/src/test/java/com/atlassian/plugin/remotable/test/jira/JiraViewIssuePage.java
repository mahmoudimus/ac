package com.atlassian.plugin.remotable.test.jira;

import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;

/**
 * A ViewIssue page that is expected to have a panel provided by a remote plugin.
 */
public class JiraViewIssuePage extends RemotePluginEmbeddedTestPage implements Page
{

    private String issueKey;

    public static JiraViewIssuePage fromViewIssuePage(PageBinder pageBinder, ViewIssuePage viewIssuePage, String embeddedPageKey)
    {
        return pageBinder.bind(JiraViewIssuePage.class, viewIssuePage, embeddedPageKey);
    }

    public JiraViewIssuePage(String issueKey, String embeddedPageKey)
    {
        super(embeddedPageKey);
        this.issueKey = issueKey;
    }

    @Override
    public String getUrl()
    {
        return "/browse/" + issueKey;
    }


}
