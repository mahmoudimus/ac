package com.atlassian.plugin.remotable.api.service.confluence.domain;

/**
 */
public interface MutablePage
{
    void setId(long id);

    void setSpaceKey(String spaceKey);

    void setParentId(Long parentId);

    void setTitle(String title);

    void setVersion(int version);

    void setContent(String content);

    long getId();

    String getSpaceKey();

    Long getParentId();

    String getTitle();

    int getVersion();

    String getContent();
}
