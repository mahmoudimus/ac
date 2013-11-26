package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.pageobjects.PageBinder;

import javax.inject.Inject;

public class JiraAdvancedSearchPage extends AdvancedSearch
{
    @Inject
    private PageBinder pageBinder;

    public IssueNavigatorViewsMenu viewsMenu()
    {
        return pageBinder.bind(IssueNavigatorViewsMenu.class);
    }
}
