package com.atlassian.labs.remoteapps.product.confluence;

import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.XmlRpcApiScope;

import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;

/**
 *
 */
public class ReadContentScope implements ApiScope
{
    private final XmlRpcApiScope xmlrpcScope = new XmlRpcApiScope("/rpc/xmlrpc", asList(
            "confluence2.getSpaces",
            "confluence2.getSpace",
            "confluence2.exportSpace",
            "confluence2.getPages",
            "confluence2.getPage",
            "confluence2.getPageHistory",
            "confluence2.getAttachments",
            "confluence2.getAncestors",
            "confluence2.getChildren",
            "confluence2.getDescendents",
            "confluence2.getComments",
            "confluence2.getComment",
            "confluence2.renderContent",
            "confluence2.getAttachment",
            "confluence2.getAttachmentData",
            "confluence2.getBlogEntry",
            "confluence2.getBlogEntries",
            "confluence2.getBlogEntryByDayAndTitle",
            "confluence2.getBlogEntryByDateAndTitle",
            "confluence2.isWatchingPage",
            "confluence2.isWatchingSpace",
            "confluence2.getWatchersForPage",
            "confluence2.getWatchersForPage",
            "confluence2.search",
            "confluence2.getLabelsById",
            "confluence2.getMostPopularLabels",
            "confluence2.getMostPopularLabelsInSpace",
            "confluence2.getRecentlyUsedLabels",
            "confluence2.getRecentlyUsedLabelsInSpace",
            "confluence2.getSpacesWithLabel",
            "confluence2.getRelatedLabels",
            "confluence2.getRelatedLabelsInSpace",
            "confluence2.getLabelsByDetail",
            "confluence2.getLabelContentById",
            "confluence2.getLabelContentByName",
            "confluence2.getLabelContentByObject",
            "confluence2.getSpacesContainingContentWithLabel"
    ));
    @Override
    public boolean allow(HttpServletRequest request)
    {
        return xmlrpcScope.allow(request);
    }
}
