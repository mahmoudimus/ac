package com.atlassian.labs.remoteapps.api.service.confluence.domain;

/**
 */
public interface MutableUser
{
    void setName(String name);

    void setDisplayName(String displayName);

    void setEmailAddress(String emailAddress);

    String getName();

    String getDisplayName();

    String getEmailAddress();
}
