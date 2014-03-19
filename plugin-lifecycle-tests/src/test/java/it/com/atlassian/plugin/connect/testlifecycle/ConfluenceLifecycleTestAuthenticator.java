package it.com.atlassian.plugin.connect.testlifecycle;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.persistence.dao.compatibility.FindUserHelper;

/**
 * we have our own version of this because we can't use the test support installer due to it's dependency on connect modules
 */
public class ConfluenceLifecycleTestAuthenticator implements LifecycleTestAuthenticator
{

    @Override
    public void authenticateUser(String username)
    {
        ConfluenceUser user = FindUserHelper.getUserByUsername(username);
        AuthenticatedUserThreadLocal.set(user);
    }
}
