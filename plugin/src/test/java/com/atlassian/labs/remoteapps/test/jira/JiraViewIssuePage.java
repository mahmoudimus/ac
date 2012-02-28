package com.atlassian.labs.remoteapps.test.jira;

import javax.inject.Inject;

import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.labs.remoteapps.test.RemoteAppEmbeddedTestPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.AtlassianWebDriver;

/**
 * A ViewIssue page that is expected to have a panel provided by a remote app.
 */
public class JiraViewIssuePage extends RemoteAppEmbeddedTestPage implements Page
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
