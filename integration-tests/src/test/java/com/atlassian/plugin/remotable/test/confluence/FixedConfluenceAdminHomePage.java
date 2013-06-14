package com.atlassian.plugin.remotable.test.confluence;

import com.atlassian.confluence.pageobjects.page.admin.ConfluenceAdminHomePage;

public class FixedConfluenceAdminHomePage extends ConfluenceAdminHomePage
{
    @Override
    public void doWait()
    {
        // don't do that crap about checking for some js variable that only exists in confluence tests
    }
}
