package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.api.model.Depth;
import com.atlassian.confluence.api.model.Expansions;
import com.atlassian.confluence.api.model.content.Container;
import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.confluence.api.model.content.Space;
import com.atlassian.confluence.api.model.pagination.LimitedRequest;
import com.atlassian.confluence.api.model.pagination.PageResponse;
import com.atlassian.confluence.api.model.pagination.PageResponseImpl;
import com.atlassian.confluence.api.model.validation.SimpleValidationResult;
import com.atlassian.confluence.api.model.validation.ValidationResult;
import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.confluence.content.apisupport.ApiSupportProvider;
import com.atlassian.confluence.content.apisupport.ContentTypeApiSupport;
import com.atlassian.confluence.content.apisupport.CustomContentApiSupportParams;
import com.atlassian.confluence.content.apisupport.CustomContentTypeApiSupport;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.ContentConvertible;
import com.google.common.base.Predicate;

import java.util.Set;

public class ExtensibleContentTypeApiSupport extends CustomContentTypeApiSupport {
    private final ContentType contentTypeKey;
    private final ApiSupportProvider apiSupportProvider;
    private final Set<String> supportedContainerTypes;
    private final Set<String> supportedContainedTypes;

    public ExtensibleContentTypeApiSupport(
            ExtensibleContentType extensibleContentType,
            CustomContentApiSupportParams customContentApiSupportParams) {

        super(customContentApiSupportParams);

        this.contentTypeKey = ContentType.valueOf(extensibleContentType.getContentTypeKey());
        this.supportedContainerTypes = extensibleContentType.getSupportedContainerTypes();
        this.supportedContainedTypes = extensibleContentType.getSupportedContainedTypes();

        this.apiSupportProvider = customContentApiSupportParams.getProvider();
    }

    @Override
    protected boolean updateCustomContentEntity(Content content, CustomContentEntityObject customContentEntityObject, CustomContentEntityObject customContentEntityObject1) {
        return false;
    }

    @Override
    protected void createCustomContentEntity(Content content, CustomContentEntityObject customContentEntityObject) {
    }

    @Override
    protected PageResponse<Content> getChildrenForThisType(CustomContentEntityObject content, LimitedRequest limitedRequest, Expansions expansions, Depth depth) {
        return PageResponseImpl.empty(false);
    }

    @Override
    protected PageResponse<Content> getChildrenForThisType(CustomContentEntityObject content, LimitedRequest limitedRequest, Expansions expansions, Depth depth, Predicate<? super ContentEntityObject> predicate) {
        return PageResponseImpl.empty(false);
    }

    @Override
    protected PageResponse<Content> getChildrenOfThisTypeForOtherType(ContentConvertible parent, LimitedRequest limitedRequest, Expansions expansions, Depth depth) {
        return PageResponseImpl.empty(false);
    }

    @Override
    protected PageResponse<Content> getChildrenOfThisTypeForOtherType(ContentConvertible parent, LimitedRequest limitedRequest, Expansions expansions, Depth depth, Predicate<? super ContentEntityObject> predicate) {
        return PageResponseImpl.empty(false);
    }

    @Override
    public ContentType getHandledType() {
        return contentTypeKey;
    }

    @Override
    public boolean supportsChildrenOfType(ContentType contentType) {
        return supportedContainedTypes.contains(contentType.getType());
    }

    @Override
    public boolean supportsChildrenForParentType(ContentType parentType) {
        return supportedContainerTypes.contains(parentType.getType());
    }

    @Override
    public ValidationResult validateCreate(Content newContent) {
        return checkContainerSupport(newContent);
    }

    @Override
    public ValidationResult validateUpdate(Content updatedContent, CustomContentEntityObject existingEntity) {
        return checkContainerSupport(updatedContent);
    }

    private ValidationResult checkContainerSupport(Content newContent) {
        Container container = newContent.getContainer();
        SimpleValidationResult.Builder resultBuilder = SimpleValidationResult.builder();

        if (!newContent.getSpaceRef().exists()) {
            return resultBuilder.addError("You must specify a space for new extensible content.").build();
        }

        if (container == null || container instanceof Space) {
            // Check if current extensible content type can be a first class content type
            Space space = (container == null) ? newContent.getSpaceRef().get() : (Space) container;
            if (!supportedContainerTypes.contains("space") && !supportedContainerTypes.contains(space.getType().getType())) {
                resultBuilder.addError(String.format(
                        "Extensible Content Type %s can not be a child of %s space.",
                        contentTypeKey, space.getType().getType()));
            }
        } else if (container instanceof Content) {
            Content containerContent = ((Content) container);
            ContentType containerContentType = containerContent.getType();

            // Check if container type
            if (!supportsChildrenForParentType(containerContentType)) {
                resultBuilder.addError(String.format(
                        "Content type %s is not a supported container type for Extensible Content Type %s.",
                        containerContentType.getType(), contentTypeKey));
            }

            // Check if container allow the this content type to be a child
            ContentTypeApiSupport containerContentTypeSupport = apiSupportProvider.getForType(containerContentType);
            if (!containerContentTypeSupport.supportsChildrenOfType(newContent.getType())) {
                resultBuilder.addError(String.format(
                        "Extensible Content Type %s can not be a child of Content type %s.",
                        contentTypeKey, containerContentType.getType()));
            }
        } else {
            resultBuilder.addError(String.format(
                    "The container property is required when creating a %s, and it must be another Content object.",
                    contentTypeKey));
        }

        if (resultBuilder.hasErrors()) {
            return resultBuilder.authorized(true).build();
        }

        // TODO: Add permission check
        return SimpleValidationResult.VALID;
    }
}
