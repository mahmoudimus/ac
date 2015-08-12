package com.atlassian.plugin.connect.crowd.upgrade;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.model.user.User;

/**
 * Finds connect add-on users that require updating as part of
 * {@link ConnectAddOnUserAppSpecificAttributeUpgradeTask}
 *
 * <strong>Not suitable for general-purpose add-on user management</strong>
 */
public interface ConnectAddOnUsers
{
    Iterable<User> getAddonUsersToUpgradeForHostProduct()
            throws ApplicationNotFoundException;

    Iterable<User> getAddonUsersToClean()
            throws ApplicationNotFoundException;
}