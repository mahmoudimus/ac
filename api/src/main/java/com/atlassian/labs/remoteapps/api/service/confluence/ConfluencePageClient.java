package com.atlassian.labs.remoteapps.api.service.confluence;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.api.service.confluence.domain.*;
import com.atlassian.labs.remoteapps.spi.util.RequirePermission;

/**
 *
 */
public interface ConfluencePageClient
{
    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<PageSummary>> getPages(String spaceKey);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Page> getPage(long pageId);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<PageSummary> getPageSummary(long pageId);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Page> getPage(String spaceKey, String pageTitle);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<PageSummary> getPageSummary(String spaceKey, String pageTitle);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<Comment>> getComments(long pageId);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Comment> getComment(long commentId);

    @RequirePermission(ConfluencePermission.MODIFY_CONTENT)
    Promise<Comment> addComment(MutableComment comment);

    @RequirePermission(ConfluencePermission.MODIFY_CONTENT)
    Promise<Comment> editComment(MutableComment comment);

    @RequirePermission(ConfluencePermission.MODIFY_CONTENT)
    Promise<Void> removeComment(long commentId);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<PageSummary>> getDecendents(long pageId);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<PageSummary>> getTopLevelPages(String spaceKey);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<PageSummary>> getAncestors(long pageId);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<PageSummary>> getChildren(long pageId);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<Attachment>> getAttachments(long pageId);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<PageHistorySummary>> getPageHistory(long pageId);

    @RequirePermission(ConfluencePermission.MODIFY_CONTENT)
    Promise<Void> movePageToTopLevel(long pageId, String targetSpaceKey);

    @RequirePermission(ConfluencePermission.MODIFY_CONTENT)
    Promise<Void> movePage(long sourcePageId, long targetPageId, MovePagePosition position);

    @RequirePermission(ConfluencePermission.MODIFY_CONTENT)
    Promise<Void> removePage(long pageId);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<SearchResult>> search(String query, SearchOptions options, int maxResults);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<SearchResult>> search(String query, int maxResults);

    @RequirePermission(ConfluencePermission.RENDER_CONTENT)
    Promise<String> renderContent(String spaceKey, long pageId, String newContent);

    @RequirePermission(ConfluencePermission.RENDER_CONTENT)
    Promise<String> renderContent(String spaceKey, long pageId, String newContent, RenderOptions renderOptions);

    @RequirePermission(ConfluencePermission.MODIFY_CONTENT)
    Promise<Page> storePage(MutablePage page);

    @RequirePermission(ConfluencePermission.MODIFY_CONTENT)
    Promise<Page> updatePage(MutablePage page, PageUpdateOptions options);

    @RequirePermission(ConfluencePermission.MODIFY_CONTENT)
    Promise<Void> setContentPermissions(long contentId, ContentPermissionType permissionType, Iterable<MutableContentPermission> contentPermissions);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<ContentPermissionSet>> getContentPermissionSets(long contentId);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<ContentPermissionSet> getContentPermissionSet(long contentId, ContentPermissionType type);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<ContentSummaries> getTrashContents(String spaceKey, int offset, int count);

    @RequirePermission(ConfluencePermission.MODIFY_CONTENT)
    Promise<Boolean> purgeFromTrash(String spaceKey, long contentId);

    @RequirePermission(ConfluencePermission.MODIFY_CONTENT)
    Promise<Boolean> emptyTrash(String spaceKey);
}
