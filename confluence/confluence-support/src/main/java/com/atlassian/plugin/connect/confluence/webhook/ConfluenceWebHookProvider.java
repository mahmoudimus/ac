package com.atlassian.plugin.connect.confluence.webhook;

import com.atlassian.confluence.event.events.content.attachment.AttachmentCreateEvent;
import com.atlassian.confluence.event.events.content.attachment.AttachmentRemoveEvent;
import com.atlassian.confluence.event.events.content.attachment.AttachmentUpdateEvent;
import com.atlassian.confluence.event.events.content.attachment.AttachmentViewEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostCreateEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostRemoveEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostRestoreEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostTrashedEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostUpdateEvent;
import com.atlassian.confluence.event.events.content.blogpost.BlogPostViewEvent;
import com.atlassian.confluence.event.events.content.comment.CommentCreateEvent;
import com.atlassian.confluence.event.events.content.comment.CommentRemoveEvent;
import com.atlassian.confluence.event.events.content.comment.CommentUpdateEvent;
import com.atlassian.confluence.event.events.content.page.PageChildrenReorderEvent;
import com.atlassian.confluence.event.events.content.page.PageCreateEvent;
import com.atlassian.confluence.event.events.content.page.PageMoveEvent;
import com.atlassian.confluence.event.events.content.page.PageRemoveEvent;
import com.atlassian.confluence.event.events.content.page.PageRestoreEvent;
import com.atlassian.confluence.event.events.content.page.PageTrashedEvent;
import com.atlassian.confluence.event.events.content.page.PageUpdateEvent;
import com.atlassian.confluence.event.events.content.page.PageViewEvent;
import com.atlassian.confluence.event.events.follow.FollowEvent;
import com.atlassian.confluence.event.events.group.GroupCreateEvent;
import com.atlassian.confluence.event.events.group.GroupRemoveEvent;
import com.atlassian.confluence.event.events.label.LabelAddEvent;
import com.atlassian.confluence.event.events.label.LabelCreateEvent;
import com.atlassian.confluence.event.events.label.LabelDeleteEvent;
import com.atlassian.confluence.event.events.label.LabelRemoveEvent;
import com.atlassian.confluence.event.events.search.SearchPerformedEvent;
import com.atlassian.confluence.event.events.security.ContentPermissionEvent;
import com.atlassian.confluence.event.events.security.LoginEvent;
import com.atlassian.confluence.event.events.security.LoginFailedEvent;
import com.atlassian.confluence.event.events.security.LogoutEvent;
import com.atlassian.confluence.event.events.space.SpaceCreateEvent;
import com.atlassian.confluence.event.events.space.SpaceLogoUpdateEvent;
import com.atlassian.confluence.event.events.space.SpacePermissionsUpdateEvent;
import com.atlassian.confluence.event.events.space.SpaceRemoveEvent;
import com.atlassian.confluence.event.events.space.SpaceUpdateEvent;
import com.atlassian.confluence.event.events.user.UserCreateEvent;
import com.atlassian.confluence.event.events.user.UserDeactivateEvent;
import com.atlassian.confluence.event.events.user.UserReactivateEvent;
import com.atlassian.confluence.event.events.user.UserRemoveEvent;
import com.atlassian.confluence.plugins.createcontent.api.events.BlueprintPageCreateEvent;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.webhooks.spi.provider.WebHookProvider;
import com.atlassian.webhooks.spi.provider.WebHookRegistrar;

import javax.inject.Inject;

/**
 * Converts POJO events generated by Confluence into Remotable Plugin web-hooks with a JSON payload.
 */
@ExportAsService
@ConfluenceComponent
public class ConfluenceWebHookProvider implements WebHookProvider {
    private final ConfluenceEventSerializerFactory serializer;

    @Inject
    public ConfluenceWebHookProvider(ConfluenceEventSerializerFactory serializer) {
        this.serializer = serializer;
    }

    @Override
    public void provide(WebHookRegistrar publish) {
        publish.webhook("group_removed").whenFired(GroupRemoveEvent.class).serializedWith(serializer);
        publish.webhook("group_created").whenFired(GroupCreateEvent.class).serializedWith(serializer);
        publish.webhook("user_removed").whenFired(UserRemoveEvent.class).serializedWith(serializer);
        publish.webhook("user_reactivated").whenFired(UserReactivateEvent.class).serializedWith(serializer);
        publish.webhook("user_deactivated").whenFired(UserDeactivateEvent.class).serializedWith(serializer);
        publish.webhook("user_created").whenFired(UserCreateEvent.class).serializedWith(serializer);
        publish.webhook("user_followed").whenFired(FollowEvent.class).serializedWith(serializer);
        publish.webhook("space_permissions_updated").whenFired(SpacePermissionsUpdateEvent.class).serializedWith(serializer);
        publish.webhook("space_removed").whenFired(SpaceRemoveEvent.class).serializedWith(serializer);
        publish.webhook("space_logo_updated").whenFired(SpaceLogoUpdateEvent.class).serializedWith(serializer);
        publish.webhook("space_updated").whenFired(SpaceUpdateEvent.class).serializedWith(serializer);
        publish.webhook("space_created").whenFired(SpaceCreateEvent.class).serializedWith(serializer);
        publish.webhook("attachment_viewed").whenFired(AttachmentViewEvent.class).serializedWith(serializer);
        publish.webhook("attachment_removed").whenFired(AttachmentRemoveEvent.class).serializedWith(serializer);
        publish.webhook("attachment_updated").whenFired(AttachmentUpdateEvent.class).serializedWith(serializer);
        publish.webhook("attachment_created").whenFired(AttachmentCreateEvent.class).serializedWith(serializer);
        publish.webhook("label_deleted").whenFired(LabelDeleteEvent.class).serializedWith(serializer);
        publish.webhook("label_removed").whenFired(LabelRemoveEvent.class).serializedWith(serializer);
        publish.webhook("label_added").whenFired(LabelAddEvent.class).serializedWith(serializer);
        publish.webhook("label_created").whenFired(LabelCreateEvent.class).serializedWith(serializer);
        publish.webhook("search_performed").whenFired(SearchPerformedEvent.class).serializedWith(serializer);
        publish.webhook("comment_removed").whenFired(CommentRemoveEvent.class).serializedWith(serializer);
        publish.webhook("comment_updated").whenFired(CommentUpdateEvent.class).serializedWith(serializer);
        publish.webhook("comment_created").whenFired(CommentCreateEvent.class).serializedWith(serializer);
        publish.webhook("login_failed").whenFired(LoginFailedEvent.class).serializedWith(serializer);
        publish.webhook("login").whenFired(LoginEvent.class).serializedWith(serializer);
        publish.webhook("logout").whenFired(LogoutEvent.class).serializedWith(serializer);
        publish.webhook("blog_created").whenFired(BlogPostCreateEvent.class).serializedWith(serializer);
        publish.webhook("blog_removed").whenFired(BlogPostRemoveEvent.class).serializedWith(serializer);
        publish.webhook("blog_trashed").whenFired(BlogPostTrashedEvent.class).serializedWith(serializer);
        publish.webhook("blog_restored").whenFired(BlogPostRestoreEvent.class).serializedWith(serializer);
        publish.webhook("blog_updated").whenFired(BlogPostUpdateEvent.class).serializedWith(serializer);
        publish.webhook("blog_viewed").whenFired(BlogPostViewEvent.class).serializedWith(serializer);
        publish.webhook("page_updated").whenFired(PageUpdateEvent.class).serializedWith(serializer);
        publish.webhook("page_created").whenFired(PageCreateEvent.class).serializedWith(serializer);
        publish.webhook("page_removed").whenFired(PageRemoveEvent.class).serializedWith(serializer);
        publish.webhook("page_trashed").whenFired(PageTrashedEvent.class).serializedWith(serializer);
        publish.webhook("page_restored").whenFired(PageRestoreEvent.class).serializedWith(serializer);
        publish.webhook("page_moved").whenFired(PageMoveEvent.class).serializedWith(serializer);
        publish.webhook("page_viewed").whenFired(PageViewEvent.class).serializedWith(serializer);
        publish.webhook("page_children_reordered").whenFired(PageChildrenReorderEvent.class).serializedWith(serializer);
        publish.webhook("blueprint_page_created").whenFired(BlueprintPageCreateEvent.class).serializedWith(serializer);
        publish.webhook("content_permissions_updated").whenFired(ContentPermissionEvent.class).matchedBy(new NonEmptyContentPermissionEventMatcher()).serializedWith(serializer);
    }
}
