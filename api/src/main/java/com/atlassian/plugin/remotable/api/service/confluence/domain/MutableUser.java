package com.atlassian.plugin.remotable.api.service.confluence.domain;

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
