package com.atlassian.plugin.connect.plugin.iframe.context.confluence;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.connect.plugin.iframe.context.HashMapModuleContextParameters;

/**
 *
 */
public class ConfluenceModuleContextParametersImpl extends HashMapModuleContextParameters implements ConfluenceModuleContextParameters
{
    @Override
    public void addPage(final AbstractPage page)
    {
        if (page != null)
        {
            put(ConfluenceModuleContextFilter.PAGE_ID, Long.toString(page.getId()));
            put(ConfluenceModuleContextFilter.PAGE_VERSION, Long.toString(page.getVersion()));
            put(ConfluenceModuleContextFilter.PAGE_TYPE, page.getType());
            addSpace(page.getSpace());
        }
    }

    @Override
    public void addSpace(final Space space)
    {
        if (space != null)
        {
            put(ConfluenceModuleContextFilter.SPACE_KEY, space.getKey());
            put(ConfluenceModuleContextFilter.SPACE_ID, Long.toString(space.getId()));
        }
    }
}
