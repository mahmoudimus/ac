package com.atlassian.labs.remoteapps.test.confluence;

import com.atlassian.confluence.pageobjects.page.DashboardPage;

/**
 * Created by IntelliJ IDEA. User: mrdon Date: 7/03/12 Time: 3:15 AM To change this template use
 * File | Settings | File Templates.
 */
public class FixedConfluenceDashboardPage extends DashboardPage
{
    @Override
    public void doWait()
    {
        // don't do that crap about checking for some js variable that only exists in confluence tests
    }
}
