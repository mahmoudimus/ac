package com.atlassian.plugin.remotable.api.service.confluence.domain;

import com.atlassian.plugin.remotable.spi.util.RemoteName;

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
