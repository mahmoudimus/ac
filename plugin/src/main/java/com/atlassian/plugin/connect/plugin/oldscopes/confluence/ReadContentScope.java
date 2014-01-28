package com.atlassian.plugin.connect.plugin.oldscopes.confluence;

import com.atlassian.plugin.connect.api.confluence.ConfluencePermissions;
import com.atlassian.plugin.connect.spi.permission.scope.PathScopeHelper;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

public final class ReadContentScope extends ConfluenceScope
{
    public ReadContentScope()
    {
        super(ConfluencePermissions.READ_CONTENT,
                asList(
                        "exportSite",
                        "getAttachment",
                        "getAttachmentData",
                        "getBlogEntryByDateAndTitle",
                        "getBlogEntry",
                        "getBlogEntries",
                        "getLabelsById",
                        "getMostPopularLabels",
                        "getMostPopularLabelsInSpace",
                        "getLabelContentById",
                        "getLabelContentByName",
                        "getLabelContentByObject",
                        "getRecentlyUsedLabels",
                        "getRecentlyUsedLabelsInSpace",
                        "getSpacesWithLabel",
                        "getRelatedLabels",
                        "getRelatedLabelsInSpace",
                        "getSpacesContainingContentWithLabel",
                        "getLabelsByDetail",
                        "isWatchingPage",
                        "getWatchersForPage",
                        "isWatchingSpace",
                        "isWatchingSpaceForType",
                        "getWatchersForSpace",
                        "getPages",
                        "getPage",
                        "getPageSummary",
                        "getPage",
                        "getPageSummary",
                        "getComments",
                        "getComment",
                        "getDecendents",
                        "getTopLevelPages",
                        "getAncestors",
                        "getChildren",
                        "getAttachments",
                        "getPageHistory",
                        "search",
                        "search",
                        "getContentPermissionSets",
                        "getContentPermissionSet",
                        "getTrashContents",
                        "getSpaceStatus",
                        "exportSpace",
                        "exportSpace",
                        "getSpaces",
                        "getSpace",
                        "getPermissions",
                        "getPermissions",
                        "getSpaceLevelPermissions"
                ),
                asList(
                        new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/notification", asList("get")),
                        new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/status", asList("get")),
                        new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/task", asList("get")),
                        new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/search", asList("get")),
                        new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/content", asList("get")),
                        new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/attachment", asList("get")),
                        new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/breadcrumb", asList("get")),
                        new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/space", asList("get")),
                        new RestApiScopeHelper.RestScope("ui", asList("1", "1.0", "latest"), "/content", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("1", "1.0", "latest"), "/content", asList("get"))
                ),
                new PathScopeHelper(false, "/download/temp/")
        );
    }
}
