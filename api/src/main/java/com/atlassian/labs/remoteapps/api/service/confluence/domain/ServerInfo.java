package com.atlassian.labs.remoteapps.api.service.confluence.domain;

import java.net.URI;

/**
 */
public interface ServerInfo
{
    void setMajorVersion(int majorVersion);

    void setMinorVersion(int minorVersion);

    void setPatchLevel(int patchLevel);

    void setBuildId(String buildId);

    void setDevelopmentBuild(boolean developmentBuild);

    void setBaseUrl(URI baseUrl);
}
