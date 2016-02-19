package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.content.ContentEntityAdapterParent;
import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.confluence.core.BodyType;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.fugue.Option;

public class ExtensibleContentEntityAdapter extends ContentEntityAdapterParent
{
    private final ContentTypeMapper mapper;

    public ExtensibleContentEntityAdapter(ContentTypeMapper mapper)
    {
        this.mapper = mapper;
    }

    @Override
    public Option<String> getUrlPath(final CustomContentEntityObject pluginContentEntityObject)
    {
        return Option.some("/rest/api/content/" + pluginContentEntityObject.getId());
    }

    @Override
    public Option<String> getDisplayTitle(final CustomContentEntityObject pluginContentEntityObject)
    {
        return Option.none();
    }

    @Override
    public Option<String> getNameForComparison(final CustomContentEntityObject pluginContentEntityObject)
    {
        return Option.none();
    }

    @Override
    public Option<String> getAttachmentsUrlPath(final CustomContentEntityObject pluginContentEntityObject)
    {
        return Option.none();
    }

    @Override
    public Option<String> getAttachmentUrlPath(final CustomContentEntityObject pluginContentEntityObject, final Attachment attachment)
    {
        return Option.none();
    }

    @Override
    public BodyType getDefaultBodyType(final CustomContentEntityObject pluginContentEntityObject)
    {
        return BodyType.XHTML;
    }

    @Override
    public Option<String> getExcerpt(final CustomContentEntityObject pluginContentEntityObject)
    {
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
    public boolean isIndexable(final CustomContentEntityObject pluginContentEntityObject, final boolean isDefaultIndexable)
    {
        return pluginContentEntityObject.isLatestVersion() && pluginContentEntityObject.isCurrent();
    }
}
