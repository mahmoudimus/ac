package com.atlassian.labs.remoteapps.product.confluence;

import static java.util.Arrays.asList;

/**
 * API Scope for Confluence that grants Remote Apps the ability to change the details of user accounts in Confluence.
 */
public class ModifyUsersScope extends ConfluenceScope
{
    protected ModifyUsersScope()
    {
        super(asList(
                "addProfilePicture"
                // TODO: Additional methods could be added here in future, once Remote Apps are not able to be installed by random Joe Bloggs Users
        ));
    }

    @Override
    public String getKey()
    {
        return "modify_users";
    }

    @Override
    public String getName()
    {
        return "Modify Users";
    }

    @Override
    public String getDescription()
    {
        return "Change users' profile pictures";
    }
}
