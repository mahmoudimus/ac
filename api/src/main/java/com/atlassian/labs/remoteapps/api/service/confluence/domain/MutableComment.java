package com.atlassian.labs.remoteapps.api.service.confluence.domain;

/**
 */
public interface MutableComment
{
    void setId(long id);

    void setPageId(long pageId);

    void setContent(String content);

    long getId();

    long getPageId();

    String getContent();
}
