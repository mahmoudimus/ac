package com.atlassian.plugin.remotable.api.service.confluence.domain;

/**
 */
public interface SearchOptions
{
    void setSpaceKey(String spaceKey);

    void setType(ContentType type);

    void setModified(DateRange modified);

    void setContributorName(String contributorName);
}
