package com.atlassian.plugin.remotable.api.service.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.domain.BlogEntry;
import com.atlassian.plugin.remotable.api.service.confluence.domain.BlogEntrySummary;
import com.atlassian.plugin.remotable.api.service.confluence.domain.MutableBlogEntry;
import com.atlassian.plugin.remotable.spi.util.RequirePermission;
import com.atlassian.util.concurrent.Promise;

/**
 */
public interface ConfluenceBlogClient
{
    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<BlogEntry> getBlogEntryByDateAndTitle(String spaceKey, int year, int month, int dayOfMoth, String postTitle);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<BlogEntry> getBlogEntry(long entryId);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<BlogEntrySummary> getBlogEntries(String spaceKey);

    @RequirePermission(ConfluencePermission.MODIFY_CONTENT)
    Promise<BlogEntry> storeBlogEntry(MutableBlogEntry blogEntry);
}
