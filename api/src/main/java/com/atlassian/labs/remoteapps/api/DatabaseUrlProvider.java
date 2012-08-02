package com.atlassian.labs.remoteapps.api;

/**
 * An interface for the UB container to access the database URL. The provided URL must be a valid
 * JDBC URL <strong>with</strong> credentials.
 */
public interface DatabaseUrlProvider
{
    String getUrl();
}
