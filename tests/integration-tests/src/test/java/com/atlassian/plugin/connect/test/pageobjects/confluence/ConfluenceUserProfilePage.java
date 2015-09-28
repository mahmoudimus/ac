package com.atlassian.plugin.connect.test.pageobjects.confluence;

/**
 * User Profile confluence page.
 */
public class ConfluenceUserProfilePage extends ConfluenceBasePage
{
    @Override
    public String getUrl()
    {
        return "/users/viewmyprofile.action";
    }

}
