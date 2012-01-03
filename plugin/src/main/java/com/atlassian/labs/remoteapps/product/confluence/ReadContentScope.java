package com.atlassian.labs.remoteapps.product.confluence;

import static java.util.Arrays.asList;

/**
 *
 */
public class ReadContentScope extends ConfluenceScope
{
    public ReadContentScope()
    {
        super(asList(
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
        ));
    }
}
