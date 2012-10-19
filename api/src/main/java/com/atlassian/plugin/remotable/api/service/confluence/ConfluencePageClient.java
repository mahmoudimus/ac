package com.atlassian.plugin.remotable.api.service.confluence;

import com.atlassian.plugin.remotable.api.service.confluence.domain.Attachment;
import com.atlassian.plugin.remotable.api.service.confluence.domain.Comment;
import com.atlassian.plugin.remotable.api.service.confluence.domain.ContentPermissionSet;
import com.atlassian.plugin.remotable.api.service.confluence.domain.ContentPermissionType;
import com.atlassian.plugin.remotable.api.service.confluence.domain.ContentSummaries;
import com.atlassian.plugin.remotable.api.service.confluence.domain.MovePagePosition;
import com.atlassian.plugin.remotable.api.service.confluence.domain.MutableComment;
import com.atlassian.plugin.remotable.api.service.confluence.domain.MutableContentPermission;
import com.atlassian.plugin.remotable.api.service.confluence.domain.MutablePage;
import com.atlassian.plugin.remotable.api.service.confluence.domain.Page;
import com.atlassian.plugin.remotable.api.service.confluence.domain.PageHistorySummary;
import com.atlassian.plugin.remotable.api.service.confluence.domain.PageSummary;
import com.atlassian.plugin.remotable.api.service.confluence.domain.PageUpdateOptions;
import com.atlassian.plugin.remotable.api.service.confluence.domain.RenderOptions;
import com.atlassian.plugin.remotable.api.service.confluence.domain.SearchOptions;
import com.atlassian.plugin.remotable.api.service.confluence.domain.SearchResult;
import com.atlassian.plugin.remotable.spi.util.RequirePermission;
import com.atlassian.util.concurrent.Promise;

/**
 *
 */
public interface ConfluencePageClient
{
    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<PageSummary>> getPages(String spaceKey);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Page> getPage(long pageId);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<PageSummary> getPageSummary(long pageId);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Page> getPage(String spaceKey, String pageTitle);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<PageSummary> getPageSummary(String spaceKey, String pageTitle);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<Comment>> getComments(long pageId);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Comment> getComment(long commentId);

    @RequirePermission(ConfluencePermissions.MODIFY_CONTENT)
    Promise<Comment> addComment(MutableComment comment);

    @RequirePermission(ConfluencePermissions.MODIFY_CONTENT)
    Promise<Comment> editComment(MutableComment comment);

    @RequirePermission(ConfluencePermissions.MODIFY_CONTENT)
    Promise<Void> removeComment(long commentId);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<PageSummary>> getDecendents(long pageId);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<PageSummary>> getTopLevelPages(String spaceKey);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<PageSummary>> getAncestors(long pageId);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<PageSummary>> getChildren(long pageId);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<Attachment>> getAttachments(long pageId);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<PageHistorySummary>> getPageHistory(long pageId);

    @RequirePermission(ConfluencePermissions.MODIFY_CONTENT)
    Promise<Void> movePageToTopLevel(long pageId, String targetSpaceKey);

    @RequirePermission(ConfluencePermissions.MODIFY_CONTENT)
    Promise<Void> movePage(long sourcePageId, long targetPageId, MovePagePosition position);

    @RequirePermission(ConfluencePermissions.MODIFY_CONTENT)
    Promise<Void> removePage(long pageId);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<SearchResult>> search(String query, SearchOptions options, int maxResults);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<SearchResult>> search(String query, int maxResults);

    @RequirePermission(ConfluencePermissions.RENDER_CONTENT)
    Promise<String> renderContent(String spaceKey, long pageId, String newContent);

    @RequirePermission(ConfluencePermissions.RENDER_CONTENT)
    Promise<String> renderContent(String spaceKey, long pageId, String newContent, RenderOptions renderOptions);

    @RequirePermission(ConfluencePermissions.MODIFY_CONTENT)
    Promise<Page> storePage(MutablePage page);

    @RequirePermission(ConfluencePermissions.MODIFY_CONTENT)
    Promise<Page> updatePage(MutablePage page, PageUpdateOptions options);

    @RequirePermission(ConfluencePermissions.MODIFY_CONTENT)
    Promise<Void> setContentPermissions(long contentId, ContentPermissionType permissionType, Iterable<MutableContentPermission> contentPermissions);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<Iterable<ContentPermissionSet>> getContentPermissionSets(long contentId);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<ContentPermissionSet> getContentPermissionSet(long contentId, ContentPermissionType type);

    @RequirePermission(ConfluencePermissions.READ_CONTENT)
    Promise<ContentSummaries> getTrashContents(String spaceKey, int offset, int count);

    @RequirePermission(ConfluencePermissions.MODIFY_CONTENT)
    Promise<Boolean> purgeFromTrash(String spaceKey, long contentId);

    @RequirePermission(ConfluencePermissions.MODIFY_CONTENT)
    Promise<Boolean> emptyTrash(String spaceKey);
}
