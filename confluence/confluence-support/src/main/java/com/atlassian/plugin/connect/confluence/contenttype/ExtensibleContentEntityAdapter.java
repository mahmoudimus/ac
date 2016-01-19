package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.content.ContentEntityAdapterParent;
import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.confluence.core.BodyType;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class ExtensibleContentEntityAdapter extends ContentEntityAdapterParent
{
    private final ContentTypeMapper mapper;

    @Autowired
    public ExtensibleContentEntityAdapter(final ContentTypeMapper mapper)
    {
        this.mapper = mapper;
    }

    public static boolean isExtensibleContentType(CustomContentEntityObject o)
    {
        return o != null && ExtensibleContentTypeSupport.contentType.equals(o.getPluginModuleKey());
    }

    @Override
    public Option<String> getUrlPath(final CustomContentEntityObject pluginContentEntityObject)
    {
        return Option.some("test path");
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
    public boolean isIndexable(final CustomContentEntityObject pluginContentEntityObject, final boolean isDefaultIndexable)
    {
        return pluginContentEntityObject.isLatestVersion() && pluginContentEntityObject.isCurrent();
    }
}
