package com.atlassian.plugin.connect.plugin.product.confluence.webhook;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.content.attachment.*;
import com.atlassian.confluence.event.events.content.blogpost.*;
import com.atlassian.confluence.event.events.content.comment.*;
import com.atlassian.confluence.event.events.content.page.*;
import com.atlassian.confluence.event.events.follow.FollowEvent;
import com.atlassian.confluence.event.events.group.GroupCreateEvent;
import com.atlassian.confluence.event.events.group.GroupRemoveEvent;
import com.atlassian.confluence.event.events.label.*;
import com.atlassian.confluence.event.events.search.SearchPerformedEvent;
import com.atlassian.confluence.event.events.security.*;
import com.atlassian.confluence.event.events.space.*;
import com.atlassian.confluence.event.events.user.*;
import com.atlassian.confluence.event.events.userstatus.*;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.webhooks.api.register.WebHookEventGroup;
import com.atlassian.webhooks.api.register.WebHookPluginRegistration;
import com.atlassian.webhooks.spi.WebHookPluginRegistrationFactory;

import static com.atlassian.webhooks.api.register.RegisteredWebHookEvent.withId;
import static com.atlassian.webhooks.api.register.WebHookEventSection.section;
import static com.atlassian.webhooks.api.util.EventMatchers.ALWAYS_TRUE;

public final class ConfluenceWebHookPluginRegistrationFactory implements WebHookPluginRegistrationFactory
{
    private final ConfluenceEventSerializer serializer;

    public ConfluenceWebHookPluginRegistrationFactory(final ConfluenceEventSerializer serializer)
    {
        this.serializer = serializer;
    }

    @Override
    public WebHookPluginRegistration createPluginRegistration()
    {
        return WebHookPluginRegistration.builder()
                .addWebHookSection(section("confluence-webhooks").addGroup(confluenceEvents()).build())
                .eventSerializer(ConfluenceEvent.class, serializer)
                .build();
    }

    private WebHookEventGroup confluenceEvents()
    {
        return WebHookEventGroup.builder()
                .addEvent(withId("group_removed").firedWhen(GroupRemoveEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("group_created").firedWhen(GroupCreateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("user_removed").firedWhen(UserRemoveEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("user_reactivated").firedWhen(UserReactivateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("user_deactivated").firedWhen(UserDeactivateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("user_created").firedWhen(UserCreateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("user_followed").firedWhen(FollowEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("status_removed").firedWhen(StatusRemoveEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("status_cleared").firedWhen(StatusClearedEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("status_created").firedWhen(StatusCreateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("space_permissions_updated").firedWhen(SpacePermissionsUpdateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("space_removed").firedWhen(SpaceRemoveEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("space_logo_updated").firedWhen(SpaceLogoUpdateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("space_updated").firedWhen(SpaceUpdateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("space_created").firedWhen(SpaceCreateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("attachment_viewed").firedWhen(AttachmentViewEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("attachment_removed").firedWhen(AttachmentRemoveEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("attachment_updated").firedWhen(AttachmentUpdateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("attachment_created").firedWhen(AttachmentCreateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("label_deleted").firedWhen(LabelDeleteEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("label_removed").firedWhen(LabelRemoveEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("label_added").firedWhen(LabelAddEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("label_created").firedWhen(LabelCreateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("search_performed").firedWhen(SearchPerformedEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("comment_removed").firedWhen(CommentRemoveEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("comment_updated").firedWhen(CommentUpdateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("comment_created").firedWhen(CommentCreateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("login_failed").firedWhen(LoginFailedEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("login").firedWhen(LoginEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("logout").firedWhen(LogoutEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("blog_created").firedWhen(BlogPostCreateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("blog_removed").firedWhen(BlogPostRemoveEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("blog_trashed").firedWhen(BlogPostTrashedEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("blog_restored").firedWhen(BlogPostRestoreEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("blog_updated").firedWhen(BlogPostUpdateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("blog_viewed").firedWhen(BlogPostViewEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("page_updated").firedWhen(PageUpdateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("page_created").firedWhen(PageCreateEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("page_removed").firedWhen(PageRemoveEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("page_trashed").firedWhen(PageTrashedEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("page_restored").firedWhen(PageRestoreEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("page_moved").firedWhen(PageMoveEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("page_viewed").firedWhen(PageViewEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("page_children_reordered").firedWhen(PageChildrenReorderEvent.class).isMatchedBy(ALWAYS_TRUE))
                .addEvent(withId("content_permissions_updated").firedWhen(ContentPermissionEvent.class).isMatchedBy(new NonEmptyContentPermissionEventMatcher()))
                .build();
    }
}