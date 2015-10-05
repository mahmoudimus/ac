package com.atlassian.plugin.connect.testsupport.util.auth;

public interface TestAuthenticator
{
    void authenticateUser(String username);

    void unauthenticate();
}
