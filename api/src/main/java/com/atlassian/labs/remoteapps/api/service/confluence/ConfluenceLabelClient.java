package com.atlassian.labs.remoteapps.api.service.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.domain.Label;
import com.atlassian.labs.remoteapps.api.service.confluence.domain.MutableLabel;
import com.atlassian.labs.remoteapps.api.service.confluence.domain.SearchResult;
import com.atlassian.labs.remoteapps.api.service.confluence.domain.Space;
import com.atlassian.labs.remoteapps.spi.util.RequirePermission;
import com.atlassian.util.concurrent.Promise;

/**
 */
public interface ConfluenceLabelClient
{
    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<Label>> getLabelsById(long contentId);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<Label>> getMostPopularLabels(int maxCount);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<Label>> getMostPopularLabelsInSpace(String spaceKey, int maxCount);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<SearchResult>> getLabelContentById(long labelId);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<SearchResult>> getLabelContentByName(String labelName);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<SearchResult>> getLabelContentByObject(MutableLabel label);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<Label>> getRecentlyUsedLabels(int maxCount);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<Label>> getRecentlyUsedLabelsInSpace(String spaceKey, int maxCount);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<Space>> getSpacesWithLabel(String labelName);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<Label>> getRelatedLabels(String labelName, int maxCount);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<Label>> getRelatedLabelsInSpace(String labelName, String spaceKey, int maxCount);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<Space>> getSpacesContainingContentWithLabel(String labelName);

    @RequirePermission(ConfluencePermission.READ_CONTENT)
    Promise<Iterable<Label>> getLabelsByDetail(String labelName, String namespace, String spaceKey, String owner);

    @RequirePermission(ConfluencePermission.LABEL_CONTENT)
    Promise<Void> addLabelByName(String labelName, long contentId);

    @RequirePermission(ConfluencePermission.LABEL_CONTENT)
    Promise<Void> addLabelById(long labelId, long contentId);

    @RequirePermission(ConfluencePermission.LABEL_CONTENT)
    Promise<Void> addLabelByObject(MutableLabel label, long contentId);

    @RequirePermission(ConfluencePermission.LABEL_CONTENT)
    Promise<Void> addLabelByNameToSpace(String labelName, String spaceKey);

    @RequirePermission(ConfluencePermission.LABEL_CONTENT)
    Promise<Void> removeLabelByName(String labelReferences, long contentId);

    @RequirePermission(ConfluencePermission.LABEL_CONTENT)
    Promise<Void> removeLabelById(long labelId, long contentId);

    @RequirePermission(ConfluencePermission.LABEL_CONTENT)
    Promise<Void> removeLabelByObject(MutableLabel label, long contentId);

    @RequirePermission(ConfluencePermission.LABEL_CONTENT)
    Promise<Void> removeLabelByNameFromSpace(String labelName, String spaceKey);
}
