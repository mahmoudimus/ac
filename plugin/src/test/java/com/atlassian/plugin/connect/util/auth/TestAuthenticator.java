package com.atlassian.plugin.connect.util.auth;

public interface TestAuthenticator
{
    void authenticateUser(String username);

    void unauthenticate();
}
