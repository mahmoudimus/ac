package com.atlassian.connect.test.confluence.pageobjects;

import com.atlassian.pageobjects.Page;

/**
 * User Profile confluence page.
 */
public class ConfluenceUserProfilePage implements Page
{
    @Override
    public String getUrl()
    {
        return "/users/viewmyprofile.action";
    }

}
