package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import com.atlassian.labs.remoteapps.spi.util.RemoteName;

import java.net.URI;

/**
 */
public interface User
{
    String getName();

    @RemoteName("fullname")
    String getDisplayName();

    @RemoteName("email")
    String getEmailAddress();

    URI getUrl();
}
