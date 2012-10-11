package com.atlassian.plugin.remotable.api.service.confluence.domain;

/**
 */
class SearchOptionsImpl implements SearchOptions
{
    private String spaceKey;
    private ContentType type;
    private DateRange modified;
    private String contributorName;

    public String getSpaceKey()
    {
        return spaceKey;
    }

    @Override
    public void setSpaceKey(String spaceKey)
    {
        this.spaceKey = spaceKey;
    }

    public ContentType getType()
    {
        return type;
    }

    @Override
    public void setType(ContentType type)
    {
        this.type = type;
    }

    public DateRange getModified()
    {
        return modified;
    }

    @Override
    public void setModified(DateRange modified)
    {
        this.modified = modified;
    }

    public String getContributorName()
    {
        return contributorName;
    }

    @Override
    public void setContributorName(String contributorName)
    {
        this.contributorName = contributorName;
    }
}
