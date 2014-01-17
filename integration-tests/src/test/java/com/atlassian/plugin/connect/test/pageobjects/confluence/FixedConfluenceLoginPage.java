package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.page.ConfluenceLoginPage;

public class FixedConfluenceLoginPage extends ConfluenceLoginPage
{
    @Override
    public void doWait()
    {
        // don't do that crap about checking for some js variable that only exists in confluence tests
    }
}
