package com.atlassian.labs.remoteapps.api.service.confluence.domain;

/**
 */
class LabelImpl implements MutableLabel
{
    private long id;

    @Override
    public long getId()
    {
        return id;
    }

    @Override
    public void setId(long id)
    {
        this.id = id;
    }
}
