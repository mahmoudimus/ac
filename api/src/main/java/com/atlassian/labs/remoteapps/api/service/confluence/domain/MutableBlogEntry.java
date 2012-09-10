package com.atlassian.labs.remoteapps.api.service.confluence.domain;

/**
 */
public interface MutableBlogEntry
{
    void setId(long id);

    void setSpaceKey(String spaceKey);

    void setTitle(String title);

    void setContent(String content);

    long getId();

    String getSpaceKey();

    String getTitle();

    String getContent();
}
