package it.com.atlassian.plugin.connect;

public interface TestAuthenticator
{
    void authenticateUser(String username);

    void unauthenticate();
}
