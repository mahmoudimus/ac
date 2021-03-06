package it.com.atlassian.plugin.connect.confluence.util;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.persistence.dao.compatibility.FindUserHelper;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;

public class ConfluenceTestAuthenticator implements TestAuthenticator {

    @Override
    public void authenticateUser(String username) {
        ConfluenceUser user = FindUserHelper.getUserByUsername(username);
        AuthenticatedUserThreadLocal.set(user);
    }

    @Override
    public void unauthenticate() {
        AuthenticatedUserThreadLocal.set(null);
    }
}
