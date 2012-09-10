package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

import java.util.Date;

/**
 */
public interface UserInformation
{
    long getId();

    @RemoteName("username")
    String getUserName();

    @RemoteName("content")
    String getDescription();

    int getVersion();

    String getCreatorName();

    @RemoteName("creationDate")
    Date getCreated();

    @RemoteName("lastModifierName")
    String getModifierName();

    @RemoteName("lastModificationDate")
    Date getModified();
}
