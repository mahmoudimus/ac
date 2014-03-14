package it.com.atlassian.plugin.connect.testlifecycle;

/**
 * we have our own version of this because we can't use the test support installer due to it's dependency on connect modules
 */
public interface LifecycleTestAuthenticator
{
    void authenticateUser(String username);
}
