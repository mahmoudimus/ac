package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.content.ContentEntityAdapterParent;
import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.confluence.core.BodyType;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;

public class ExtensibleContentEntityAdapter extends ContentEntityAdapterParent {
    private final boolean indexingEnabled;
    private final BodyType bodyType;

    public ExtensibleContentEntityAdapter(ExtensibleContentTypeModuleBean bean) {
        this.indexingEnabled = bean.getApiSupport().getIndexing().isEnabled();

        switch (bean.getApiSupport().getBodyType()) {
            case WIKI:
                this.bodyType = BodyType.WIKI;
                break;
            case RAW:
                this.bodyType = BodyType.RAW;
                break;
            default:
                this.bodyType = BodyType.XHTML;
                break;
        }
    }

    @Override
    public Option<String> getUrlPath(final CustomContentEntityObject pluginContentEntityObject) {
        return Option.some("/rest/api/content/" + pluginContentEntityObject.getId());
    }

    @Override
    public Option<String> getDisplayTitle(final CustomContentEntityObject pluginContentEntityObject) {
        return Option.none();
    }

    @Override
    public Option<String> getNameForComparison(final CustomContentEntityObject pluginContentEntityObject) {
        return Option.none();
    }

    @Override
    public Option<String> getAttachmentsUrlPath(final CustomContentEntityObject pluginContentEntityObject) {
        return Option.none();
    }

    @Override
    public Option<String> getAttachmentUrlPath(final CustomContentEntityObject pluginContentEntityObject, final Attachment attachment) {
        return Option.none();
    }

    @Override
    public BodyType getDefaultBodyType(final CustomContentEntityObject pluginContentEntityObject) {
        return bodyType;
    }

    @Override
    public Option<String> getExcerpt(final CustomContentEntityObject pluginContentEntityObject) {
        return Option.none();
    }

    @Override
    public boolean isAllowedParent(CustomContentEntityObject child, CustomContentEntityObject parent) {
        return true;
    }

    @Override
    public boolean isAllowedContainer(ContentEntityObject child, ContentEntityObject container) {
        return true;
    }

    @Override
    public boolean isIndexable(final CustomContentEntityObject pluginContentEntityObject, final boolean isDefaultIndexable) {
        return indexingEnabled && pluginContentEntityObject.isLatestVersion() && pluginContentEntityObject.isCurrent();
    }
}
