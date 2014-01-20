package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.page.admin.templates.SpaceTemplatesPage;

public class FixedSpaceTemplatesPage extends SpaceTemplatesPage
{
    public FixedSpaceTemplatesPage(String spaceKey) {
        super(spaceKey);
    }

    @Override
    public void doWait()
    {
        // don't do that crap about checking for some js variable that only exists in confluence tests
    }
}
