package com.atlassian.labs.remoteapps.api.service.confluence.domain;

/**
 */
public interface PageUpdateOptions
{
    void setVersionComment(String versionComment);

    void setMinorEdit(boolean minorEdit);
}
