package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.api.model.Depth;
import com.atlassian.confluence.api.model.Expansions;
import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.confluence.api.model.pagination.LimitedRequest;
import com.atlassian.confluence.api.model.pagination.PageResponse;
import com.atlassian.confluence.api.model.validation.SimpleValidationResult;
import com.atlassian.confluence.api.model.validation.ValidationResult;
import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.confluence.content.apisupport.CustomContentApiSupportParams;
import com.atlassian.confluence.content.apisupport.CustomContentTypeApiSupport;
import com.atlassian.confluence.pages.ContentConvertible;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;

public class ExtensibleContentTypeSupport extends CustomContentTypeApiSupport
{
    private final ContentType contentType;

    public ExtensibleContentTypeSupport(
            String contentTypeKey,
            ExtensibleContentTypeModuleBean bean,
            CustomContentApiSupportParams params)
    {
        super(params);
        this.contentType = ContentType.valueOf(contentTypeKey);
    }

    @Override
    protected boolean updateCustomContentEntity(Content content, CustomContentEntityObject customContentEntityObject, CustomContentEntityObject customContentEntityObject1)
    {
        return false;
    }

    @Override
    protected void createCustomContentEntity(Content content, CustomContentEntityObject customContentEntityObject)
    {

    }

    @Override
    protected PageResponse<Content> getChildrenForThisType(CustomContentEntityObject customContentEntityObject, LimitedRequest limitedRequest, Expansions expansions, Depth depth)
    {
        return null;
    }

    @Override
    protected PageResponse<Content> getChildrenOfThisTypeForOtherType(ContentConvertible contentConvertible, LimitedRequest limitedRequest, Expansions expansions, Depth depth)
    {
        return null;
    }

    @Override
    public ContentType getHandledType()
    {
        return contentType;
    }

    @Override
    public boolean supportsChildrenOfType(ContentType contentType)
    {
        return false;
    }

    @Override
    public boolean supportsChildrenForParentType(ContentType contentType)
    {
        return false;
    }

    @Override
    public ValidationResult validateCreate(Content newContent) {
        return SimpleValidationResult.VALID;
    }

    @Override
    public ValidationResult validateUpdate(Content updatedContent, CustomContentEntityObject existingEntity) {
        return SimpleValidationResult.VALID;
    }
}
