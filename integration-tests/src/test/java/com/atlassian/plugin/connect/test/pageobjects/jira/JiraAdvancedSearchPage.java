package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import javax.inject.Inject;

public class JiraAdvancedSearchPage extends AdvancedSearch
{
    @Inject
    private PageBinder pageBinder;

    public IssueNavigatorViewsMenu viewsMenu()
    {
        return pageBinder.bind(IssueNavigatorViewsMenu.class);
    }

    @Override
    public TimedCondition isAt()
    {
        Poller.waitUntilTrue(searchButton.timed().isVisible()); // make sure the page is in it's final state (ACDEV-684)
        return super.isAt();
    }

}
