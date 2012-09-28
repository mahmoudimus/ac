package com.atlassian.labs.remoteapps.api.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.domain.BlogEntry;
import com.atlassian.labs.remoteapps.api.service.confluence.domain.BlogEntrySummary;
import com.atlassian.labs.remoteapps.api.service.confluence.domain.MutableBlogEntry;
import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.spi.util.RequirePermission;

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
