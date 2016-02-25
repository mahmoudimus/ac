package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.api.impl.service.content.typebinding.TreeSorter;
import com.atlassian.confluence.api.model.Depth;
import com.atlassian.confluence.api.model.Expansions;
import com.atlassian.confluence.api.model.content.Container;
import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.api.model.content.ContentType;
import com.atlassian.confluence.api.model.content.Space;
import com.atlassian.confluence.api.model.pagination.LimitedRequest;
import com.atlassian.confluence.api.model.pagination.PageResponse;
import com.atlassian.confluence.api.model.pagination.PageResponseImpl;
import com.atlassian.confluence.api.model.pagination.PaginationBatch;
import com.atlassian.confluence.api.model.validation.SimpleValidationResult;
import com.atlassian.confluence.api.model.validation.ValidationResult;
import com.atlassian.confluence.api.service.content.ContentService;
import com.atlassian.confluence.api.service.pagination.PaginationService;
import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.confluence.content.CustomContentManager;
import com.atlassian.confluence.content.apisupport.ApiSupportProvider;
import com.atlassian.confluence.content.apisupport.ContentTypeApiSupport;
import com.atlassian.confluence.content.apisupport.CustomContentApiSupportParams;
import com.atlassian.confluence.content.apisupport.CustomContentTypeApiSupport;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.internal.pagination.SubListResponse;
import com.atlassian.confluence.pages.ContentConvertible;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ExtensibleContentTypeSupport extends CustomContentTypeApiSupport {
    private final ContentType contentTypeKey;
    private final CustomContentManager customContentManager;
    private final PaginationService paginationService;
    private final ContentService contentService;
    private final PermissionDelegate permissionDelegate;
    private final ApiSupportProvider apiSupportProvider;
    private final Set<String> supportedContainerTypes;
    private final Set<String> supportedContainedTypes;

    public ExtensibleContentTypeSupport(
            ExtensibleContentType extensibleContentType,
            CustomContentApiSupportParams customContentApiSupportParams,
            PaginationService paginationService,
            ContentService contentService) {

        super(customContentApiSupportParams);

        this.contentTypeKey = ContentType.valueOf(extensibleContentType.getContentTypeKey());
        this.paginationService = paginationService;
        this.contentService = contentService;
        this.permissionDelegate = extensibleContentType.getPermissionDelegate();
        this.supportedContainerTypes = extensibleContentType.getSupportedContainerTypes();
        this.supportedContainedTypes = extensibleContentType.getSupportedContainedTypes();

        this.customContentManager = customContentApiSupportParams.getCustomContentManager();
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
        return getChildrenForThisType(content, limitedRequest, expansions, depth, Predicates.alwaysTrue());
    }

    @Override
    protected PageResponse<Content> getChildrenForThisType(CustomContentEntityObject content, LimitedRequest limitedRequest, Expansions expansions, Depth depth, Predicate<? super ContentEntityObject> predicate) {
        return getChildrenInternal(content, limitedRequest, expansions, depth, predicate);

    }

    @Override
    protected PageResponse<Content> getChildrenOfThisTypeForOtherType(ContentConvertible parent, LimitedRequest limitedRequest, Expansions expansions, Depth depth) {
        return getChildrenOfThisTypeForOtherType(parent, limitedRequest, expansions, depth, Predicates.alwaysTrue());
    }

    @Override
    protected PageResponse<Content> getChildrenOfThisTypeForOtherType(ContentConvertible parent, LimitedRequest limitedRequest, Expansions expansions, Depth depth, Predicate<? super ContentEntityObject> predicate) {
        ContentEntityObject other = customContentManager.getById(parent.getContentId().asLong());
        return getChildrenInternal(other, limitedRequest, expansions, depth, predicate);
    }

    private PageResponse<Content> getChildrenInternal(
            final ContentEntityObject parentCEO,
            LimitedRequest limitedRequest,
            Expansions expansions,
            Depth depth,
            Predicate<? super ContentEntityObject> predicate) {

        // Check if current user can view
        if (!permissionDelegate.canView(AuthenticatedUserThreadLocal.get(), parentCEO)) {
            return PageResponseImpl.empty(false);
        }

        PaginationBatch<CustomContentEntityObject> fetchPage;
        String handledType = getHandledType().getType();

        if (parentCEO instanceof CustomContentEntityObject) {
            // Get children for Extensible Content Type
            CustomContentEntityObject parentCCEO = (CustomContentEntityObject) parentCEO;

            fetchPage = nextRequest -> {
                List<CustomContentEntityObject> children = Lists.newLinkedList();

                // Get all children count
                long count = customContentManager.countChildrenOfType(parentCCEO, handledType);

                // Get children subset according to nextRequest
                Iterator<CustomContentEntityObject> childrenIterator = customContentManager.findChildrenOfType(
                        parentCCEO,
                        handledType,
                        nextRequest.getStart(),
                        nextRequest.getLimit(),
                        CustomContentManager.SortField.CREATED,
                        CustomContentManager.SortOrder.DESC);
                Iterators.addAll(children, childrenIterator);

                // Check if has next batch
                boolean hasNext = nextRequest.getStart() + nextRequest.getLimit() < count;
                return PageResponseImpl.from(children, hasNext).build();
            };
        } else {
            // Get Extensible Content Type children for other type
            fetchPage = nextRequest -> {
                List<CustomContentEntityObject> children = Lists.newLinkedList();
                Iterator<CustomContentEntityObject> childrenIterator = customContentManager.findAllContainedOfType(parentCEO.getContentId().asLong(), handledType);
                Iterators.addAll(children, childrenIterator);

                if (depth == Depth.ALL) {
                    // Traverse all the descendants
                    return getAllDescendants(children, nextRequest, predicate);
                } else {
                    return SubListResponse.from(filterCustomContentEntityObjects(children, predicate), nextRequest);
                }
            };
        }

        Function<ContentEntityObject, Content> modelConverter = entity ->
                contentService.find(expansions.toArray()).withId(entity.getContentId()).fetchOneOrNull();

        return paginationService.doPaginationRequest(limitedRequest, fetchPage, modelConverter);
    }

    private static final Function<CustomContentEntityObject, List<CustomContentEntityObject>> ANCESTORS_GETTER = input -> {
        List<CustomContentEntityObject> ancestors = Lists.newArrayList();
        while (input.getParent() != null) {
            ancestors.add(0, input.getParent());
            input = input.getParent();
        }
        return ancestors;
    };

    private PageResponse<CustomContentEntityObject> getAllDescendants(List<CustomContentEntityObject> allChildren, LimitedRequest nextRequest, Predicate predicate) {
        List<CustomContentEntityObject> creationDateSortedComments = filterCustomContentEntityObjects(allChildren, predicate);

        Comparator<CustomContentEntityObject> commentComparator = (o1, o2) -> o1.getContentId().compareTo(o2.getContentId());
        List<CustomContentEntityObject> treeSortedCCEOs = TreeSorter.depthFirstPreOrderSort(creationDateSortedComments, ANCESTORS_GETTER, commentComparator);

        return SubListResponse.from(treeSortedCCEOs, nextRequest);
    }

    private List<CustomContentEntityObject> filterCustomContentEntityObjects(List<CustomContentEntityObject> customContentEntityObjects, Predicate predicate) {
        if (predicate == null) {
            return customContentEntityObjects;
        }
        return ImmutableList.copyOf(Iterables.filter(customContentEntityObjects, predicate));
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
            if (!supportedContainerTypes.contains(space.getType().getType())) {
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
