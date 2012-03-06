package com.atlassian.labs.remoteapps.test.confluence;

import com.atlassian.confluence.pageobjects.page.ConfluenceLoginPage;

/**
 * Created by IntelliJ IDEA. User: mrdon Date: 7/03/12 Time: 3:11 AM To change this template use
 * File | Settings | File Templates.
 */
public class FixedConfluenceLoginPage extends ConfluenceLoginPage
{
    @Override
    public void doWait()
    {
        // don't do that crap about checking for some js variable that only exists in confluence tests
    }
}
