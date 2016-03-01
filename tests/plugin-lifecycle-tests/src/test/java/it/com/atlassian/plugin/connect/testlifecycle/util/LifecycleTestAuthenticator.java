package it.com.atlassian.plugin.connect.testlifecycle.util;

/**
 * we have our own version of this because we can't use the test support lifecycle due to it's dependency on connect modules
 */
public interface LifecycleTestAuthenticator {
    void authenticateUser(String username);
}
