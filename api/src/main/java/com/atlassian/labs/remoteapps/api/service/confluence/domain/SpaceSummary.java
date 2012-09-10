package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import java.net.URI;

/**
 */
public interface SpaceSummary
{
    String getName();

    String getKey();

    URI getUrl();

    SpaceType getType();
}
