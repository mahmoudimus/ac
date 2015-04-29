package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.Spaced;

public class ContentEntityForTests extends ContentEntityObject implements Spaced
{
    private final Space space;
    private final String pageType;
    private final String pageId;
    private final String pageTitle;
    private final int version;

    public ContentEntityForTests(String pageType, String pageId, String pageTitle, String version, String spaceKey, String spaceId)
    {
        this.space = new Space(spaceKey);
        this.space.setId(Long.parseLong(spaceId));
        this.pageType = pageType;
        this.pageId = pageId;
        this.pageTitle = pageTitle;
        this.version = Integer.parseInt(version);
    }

    public ContentEntityForTests()
    {
        this("page", "111", "Page Title", "1", "sp", "222");
    }

    @Override
    public String getType()
    {
        return pageType;
    }

    @Override
    public String getIdAsString()
    {
        return pageId;
    }

    @Override
    public String getTitle()
    {
        return pageTitle;
    }

    @Override
    public int getVersion()
    {
        return version;
    }

    @Override
    public String getUrlPath()
    {
        return "";
    }

    @Override
    public String getNameForComparison()
    {
        return "";
    }

    @Override
    public Space getSpace()
    {
        return space;
    }
}
