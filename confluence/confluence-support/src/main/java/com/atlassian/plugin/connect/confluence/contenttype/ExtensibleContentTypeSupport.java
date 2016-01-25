package com.atlassian.plugin.connect.confluence.contenttype;

import java.util.Set;

import com.atlassian.confluence.api.model.Depth;
import com.atlassian.confluence.api.model.Expansions;
import com.atlassian.confluence.api.model.content.Container;
import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.confluence.api.model.pagination.LimitedRequest;
import com.atlassian.confluence.api.model.pagination.PageResponse;
import com.atlassian.confluence.api.model.validation.SimpleValidationResult;
import com.atlassian.confluence.api.model.validation.ValidationResult;
import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.confluence.content.apisupport.ApiSupportProvider;
import com.atlassian.confluence.content.apisupport.ContentTypeApiSupport;
import com.atlassian.confluence.content.apisupport.CustomContentApiSupportParams;
import com.atlassian.confluence.content.apisupport.CustomContentTypeApiSupport;
import com.atlassian.confluence.pages.ContentConvertible;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;

public class ExtensibleContentTypeSupport extends CustomContentTypeApiSupport
{
    private final ContentType contentTypeKey;
    private final Set<ContentType> supportedContainerTypes;
    private final Set<ContentType> supportedChildrenTypes;
    private final ApiSupportProvider apiSupportProvider;
    private final boolean supportCreatDirectlyUnderSpace;

    public ExtensibleContentTypeSupport(
            String contentTypeKey,
            ExtensibleContentTypeModuleBean bean,
            CustomContentApiSupportParams params,
            ApiSupportProvider apiSupportProvider)
    {
        super(params);
        this.apiSupportProvider = apiSupportProvider;
        this.contentTypeKey = ContentType.valueOf(contentTypeKey);
        this.supportedContainerTypes = ContentType.valuesOf(bean.getApiSupport().getSupportedContainerTypes());
        this.supportedChildrenTypes = ContentType.valuesOf(bean.getApiSupport().getSupportedChildrenTypes());
        this.supportCreatDirectlyUnderSpace = bean.getApiSupport().getIsDirectlyUnderSpaceSupported();
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
        return contentTypeKey;
    }

    @Override
    public boolean supportsChildrenOfType(ContentType contentType)
    {
        return supportedContainerTypes.contains(contentType);
    }

    @Override
    public boolean supportsChildrenForParentType(ContentType contentType)
    {
        return supportedChildrenTypes.contains(contentType);
    }

    @Override
    public ValidationResult validateCreate(Content newContent)
    {
        ValidationResult containerCheckResult = checkContainerSupport(newContent);
        if (containerCheckResult != null) {
            return containerCheckResult;
        }

        return SimpleValidationResult.VALID;
    }

    @Override
    public ValidationResult validateUpdate(Content updatedContent, CustomContentEntityObject existingEntity)
    {
        ValidationResult containerCheckResult = checkContainerSupport(updatedContent);
        if (containerCheckResult != null) {
            return containerCheckResult;
        }

        return SimpleValidationResult.VALID;
    }

    private ValidationResult checkContainerSupport(Content newContent)
    {

        Container container = newContent.getContainer();

        if (supportCreatDirectlyUnderSpace)
        {
            // Container property is not mandatory but if present we need to
            // ensure it is support by this extensible content type
            // and also the container type support the content as a child.
            if (container instanceof Content)
            {
                return checkValidContainer(newContent, container);
            }
        }
        else
        {
            // Container property is mandatory
            return checkValidContainer(newContent, container);
        }

        return null;
    }

    private ValidationResult checkValidContainer(Content newContent, Container container)
    {
        SimpleValidationResult.Builder resultBuilder = SimpleValidationResult.builder();

        if (!(container instanceof Content))
            return resultBuilder
                    .addError(String.format("The container property is required when creating a %s, and it must be another Content object", contentTypeKey))
                    .build();

        Content containerContent = ((Content) container);
        ContentType containerContentType = containerContent.getType();
        ContentType newContentContentType = newContent.getType();

        // Check container is supported by the content type
        if (!supportsChildrenOfType(((Content) container).getType())) {
            return resultBuilder
                    .addError(String.format("Content type %s is not a supported container type for Extensible Content Type %s", containerContentType.getType(), contentTypeKey))
                    .build();
        }

        // Check if container allow the content type being a child content
        ContentTypeApiSupport containerContentTypeSupport = apiSupportProvider.getForType(containerContentType);
        if (!containerContentTypeSupport.supportsChildrenOfType(newContentContentType)) {
            return resultBuilder
                    .addError(String.format("Extensible Content Type %s can not be a child of Content type %s", contentTypeKey, containerContentType.getType()))
                    .build();
        }

        return null;
    }
}
