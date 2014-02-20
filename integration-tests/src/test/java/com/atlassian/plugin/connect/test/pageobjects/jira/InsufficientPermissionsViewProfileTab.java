package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.pages.ViewProfileTab;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 *
 */
public class InsufficientPermissionsViewProfileTab implements ViewProfileTab
{
    @ElementBy(id = "errorMessage")
    private PageElement errorMessage;

    @Override
    public String getUrlPart()
    {
        return "my-plugin:profile-tab-panel";
    }

    @Override
    public String linkId()
    {
        return "up_profile-tab-panel_a";
    }

    @Override
    public TimedCondition isOpen()
    {
        return errorMessage.timed().isPresent();
    }

    public String getErrorMessage()
    {
        return errorMessage.getText();
    }

}
