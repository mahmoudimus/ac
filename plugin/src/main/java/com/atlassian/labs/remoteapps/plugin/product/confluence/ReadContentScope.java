package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.labs.remoteapps.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

/**
 *
 */
public class ReadContentScope extends ConfluenceScope
{
    public ReadContentScope()
    {
        super(
            asList(
                "getSpaces",
                "getSpace",
                "getPages",
                "getPage",
                "getPageHistory",
                "getAttachments",
                "getAncestors",
                "getChildren",
                "getDescendents",
                "getComments",
                "getComment",
                "renderContent",
                "getAttachment",
                "getAttachmentData",
                "getBlogEntry",
                "getBlogEntries",
                "getBlogEntryByDayAndTitle",
                "getBlogEntryByDateAndTitle",
                "isWatchingPage",
                "isWatchingSpace",
                "getWatchersForPage",
                "getWatchersForPage",
                "search",
                "getLabelsById",
                "getMostPopularLabels",
                "getMostPopularLabelsInSpace",
                "getRecentlyUsedLabels",
                "getRecentlyUsedLabelsInSpace",
                "getSpacesWithLabel",
                "getRelatedLabels",
                "getRelatedLabelsInSpace",
                "getLabelsByDetail",
                "getLabelContentById",
                "getLabelContentByName",
                "getLabelContentByObject",
                "getSpacesContainingContentWithLabel"
            ),
            asList(
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/search", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/content", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/attachment", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/breadcrumb", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/space", asList("get"))
            )
        );
    }

    @Override
    public String getKey()
    {
        return "read_content";
    }

    @Override
    public String getName()
    {
        return "Read Content";
    }

    @Override
    public String getDescription()
    {
        return "Read spaces, blogs, and pages and associated metadata";
    }
}
