package com.atlassian.labs.remoteapps.product.confluence;

import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.JsonRpcApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.XmlRpcApiScope;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static java.util.Arrays.asList;

/**
 *
 */
public class ReadContentScope implements ApiScope
{
    private final XmlRpcApiScope xmlrpcScope = new XmlRpcApiScope("/rpc/xmlrpc", methodList("confluence2."));
    private final JsonRpcApiScope jsonrpcScope = new JsonRpcApiScope("/rpc/json-rpc/confluenceservice-v2", methodList(""));

    private final List<String> methodList(String prefix)
    {
        return asList(
                prefix + "getSpaces",
                prefix + "getSpace",
                prefix + "exportSpace",
                prefix + "getPages",
                prefix + "getPage",
                prefix + "getPageHistory",
                prefix + "getAttachments",
                prefix + "getAncestors",
                prefix + "getChildren",
                prefix + "getDescendents",
                prefix + "getComments",
                prefix + "getComment",
                prefix + "renderContent",
                prefix + "getAttachment",
                prefix + "getAttachmentData",
                prefix + "getBlogEntry",
                prefix + "getBlogEntries",
                prefix + "getBlogEntryByDayAndTitle",
                prefix + "getBlogEntryByDateAndTitle",
                prefix + "isWatchingPage",
                prefix + "isWatchingSpace",
                prefix + "getWatchersForPage",
                prefix + "getWatchersForPage",
                prefix + "search",
                prefix + "getLabelsById",
                prefix + "getMostPopularLabels",
                prefix + "getMostPopularLabelsInSpace",
                prefix + "getRecentlyUsedLabels",
                prefix + "getRecentlyUsedLabelsInSpace",
                prefix + "getSpacesWithLabel",
                prefix + "getRelatedLabels",
                prefix + "getRelatedLabelsInSpace",
                prefix + "getLabelsByDetail",
                prefix + "getLabelContentById",
                prefix + "getLabelContentByName",
                prefix + "getLabelContentByObject",
                prefix + "getSpacesContainingContentWithLabel"
        );
    }

    @Override
    public boolean allow(HttpServletRequest request, String user)
    {
        return xmlrpcScope.allow(request) || jsonrpcScope.allow(request);
    }
}
