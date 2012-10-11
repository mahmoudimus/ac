package com.atlassian.plugin.remotable.api.service.confluence.domain;

/**
 */
public interface MutableSpace
{
    void setName(String name);

    void setKey(String key);

    void setDescription(String description);

    void setHomePageId(Long homePageId);

    String getName();

    String getKey();

    String getDescription();

    Long getHomePageId();
}
