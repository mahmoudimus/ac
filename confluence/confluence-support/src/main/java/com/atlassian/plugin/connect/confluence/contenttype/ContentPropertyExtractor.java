package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.bonnie.Searchable;
import com.atlassian.bonnie.search.Extractor;
import com.atlassian.confluence.api.model.content.JsonContentProperty;
import com.atlassian.confluence.api.service.content.ContentPropertyService;
import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.confluence.rest.api.model.ExpansionsParser;
import com.atlassian.confluence.user.AuthenticatedUserImpersonator;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.persistence.dao.compatibility.FindUserHelper;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.confluence.ConfluenceFeatureManager;
import com.google.common.base.Strings;
import org.apache.lucene.document.Document;

/**
 * Extracts content property specified in the descriptor and append to the default searchable text
 */
public class ContentPropertyExtractor implements Extractor {
    public static final String SYSADMIN = "sysadmin";
    public static final String ADMIN = "admin";

    private final ConfluenceFeatureManager confluenceFeatureManager;
    private final ContentPropertyService contentPropertyService;
    private final String contentPropertyKey;

    public ContentPropertyExtractor(
            ConfluenceFeatureManager confluenceFeatureManager,
            ContentPropertyService contentPropertyService,
            String contentPropertyKey) {
        this.confluenceFeatureManager = confluenceFeatureManager;
        this.contentPropertyService = contentPropertyService;
        this.contentPropertyKey = contentPropertyKey;
    }

    @Override
    public void addFields(Document document, StringBuffer defaultSearchableText, Searchable searchable) {
        if (Strings.isNullOrEmpty(contentPropertyKey)) {
            return;
        }

        if (searchable instanceof CustomContentEntityObject) {
            /**
             * ContentPropertyService checks current user permission via AuthenticatedUserThreadLocal.get()
             * However such information is not exists in the indexing thread.
             * So we use sysadmin here to ensure this content property gets indexed.
             */
            AuthenticatedUserImpersonator.REQUEST_AGNOSTIC.asUser(() ->
                    addContentPropertyToSearchableText(searchable, defaultSearchableText), getAdminUser());
        }
    }

    public Object addContentPropertyToSearchableText(Searchable searchable, StringBuffer defaultSearchableText) {
        CustomContentEntityObject ceo = (CustomContentEntityObject) searchable;

        Option<JsonContentProperty> jsonProperty = contentPropertyService
                .find(ExpansionsParser.parse(""))
                .withContentId(ceo.getContentId())
                .withPropertyKey(contentPropertyKey)
                .fetchOne();

        JsonContentProperty jsonContentProperty = jsonProperty.getOrNull();

        if (jsonContentProperty != null) {
            String value = jsonContentProperty.getValue().getValue();

            if (!Strings.isNullOrEmpty(value)) {
                defaultSearchableText.append(" ").append(jsonContentProperty.getValue().getValue());
            }
        }

        return null;
    }

    private ConfluenceUser getAdminUser() {
        return FindUserHelper.getUserByUsername(confluenceFeatureManager.isOnDemand() ? SYSADMIN : ADMIN);
    }
}
