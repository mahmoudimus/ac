package com.atlassian.plugin.remotable.test.confluence;

import com.atlassian.confluence.pageobjects.page.DashboardPage;

public class FixedConfluenceDashboardPage extends DashboardPage
{
    @Override
    public void doWait()
    {
        // don't do that crap about checking for some js variable that only exists in confluence tests
    }
}
