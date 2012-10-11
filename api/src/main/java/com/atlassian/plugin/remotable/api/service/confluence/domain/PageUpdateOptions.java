package com.atlassian.plugin.remotable.api.service.confluence.domain;

/**
 */
public interface PageUpdateOptions
{
    void setVersionComment(String versionComment);

    void setMinorEdit(boolean minorEdit);
}
