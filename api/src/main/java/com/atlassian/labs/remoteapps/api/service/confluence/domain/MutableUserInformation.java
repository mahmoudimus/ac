package com.atlassian.labs.remoteapps.api.service.confluence.domain;

/**
 */
public interface MutableUserInformation
{
    void setId(long id);

    void setUserName(String userName);

    void setDescription(String description);

    long getId();

    String getUserName();

    String getDescription();
}
