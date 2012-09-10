package com.atlassian.labs.remoteapps.api.service.confluence.domain;

/**
 */
class PageUpdateOptionsImpl implements PageUpdateOptions
{
    private String versionComment;
    private boolean minorEdit;

    public String getVersionComment()
    {
        return versionComment;
    }

    @Override
    public void setVersionComment(String versionComment)
    {
        this.versionComment = versionComment;
    }

    public boolean isMinorEdit()
    {
        return minorEdit;
    }

    @Override
    public void setMinorEdit(boolean minorEdit)
    {
        this.minorEdit = minorEdit;
    }
}
