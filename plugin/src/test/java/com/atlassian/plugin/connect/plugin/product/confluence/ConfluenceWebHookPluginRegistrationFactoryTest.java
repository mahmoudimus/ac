package com.atlassian.plugin.connect.plugin.product.confluence;

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
import com.atlassian.confluence.event.events.userstatus.StatusClearedEvent;
import com.atlassian.confluence.event.events.userstatus.StatusCreateEvent;
import com.atlassian.confluence.event.events.userstatus.StatusRemoveEvent;
import com.atlassian.plugin.connect.plugin.product.confluence.webhook.ConfluenceEventSerializer;
import com.atlassian.plugin.connect.plugin.product.confluence.webhook.ConfluenceWebHookPluginRegistrationFactory;
import com.atlassian.plugin.connect.plugin.product.confluence.webhook.NonEmptyContentPermissionEventMatcher;
import com.atlassian.webhooks.api.register.RegisteredWebHookEvent;
import com.atlassian.webhooks.api.register.WebHookPluginRegistration;
import com.atlassian.webhooks.api.util.EventMatchers;
import com.atlassian.webhooks.spi.EventMatcher;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ConfluenceWebHookPluginRegistrationFactoryTest
{
    private static class Webhook<T>
    {
        private final String id;
        private final Class<T> eventType;
        private final EventMatcher<? super T> matcher;

        private Webhook(final String id, final Class<T> eventType, final EventMatcher<? super T> matcher)
        {
            this.id = id;
            this.eventType = eventType;
            this.matcher = matcher;
        }

        @Override
        public int hashCode() {return Objects.hashCode(id, eventType, matcher);}

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj) {return true;}
            if (obj == null || getClass() != obj.getClass()) {return false;}
            final Webhook other = (Webhook) obj;
            return Objects.equal(this.id, other.id) && Objects.equal(this.eventType, other.eventType) && Objects.equal(this.matcher.getClass(), other.matcher.getClass());
        }

        @Override
        public String toString()
        {
            return "Webhook{" + "id='" + id + '\'' + ", eventType=" + eventType + ", matcher=" + matcher.getClass().getName() + '}';
        }
    }

    private final ConfluenceWebHookPluginRegistrationFactory factory = new ConfluenceWebHookPluginRegistrationFactory(mock(ConfluenceEventSerializer.class));

    private final Webhook[] expectedWebhooks = {
            webhook("group_removed", GroupRemoveEvent.class),
            webhook("group_created", GroupCreateEvent.class),
            webhook("user_removed", UserRemoveEvent.class),
            webhook("user_reactivated", UserReactivateEvent.class),
            webhook("user_deactivated", UserDeactivateEvent.class),
            webhook("user_created", UserCreateEvent.class),
            webhook("user_followed", FollowEvent.class),
            webhook("status_removed", StatusRemoveEvent.class),
            webhook("status_cleared", StatusClearedEvent.class),
            webhook("status_created", StatusCreateEvent.class),
            webhook("space_permissions_updated", SpacePermissionsUpdateEvent.class),
            webhook("space_removed", SpaceRemoveEvent.class),
            webhook("space_logo_updated", SpaceLogoUpdateEvent.class),
            webhook("space_updated", SpaceUpdateEvent.class),
            webhook("space_created", SpaceCreateEvent.class),
            webhook("attachment_viewed", AttachmentViewEvent.class),
            webhook("attachment_removed", AttachmentRemoveEvent.class),
            webhook("attachment_updated", AttachmentUpdateEvent.class),
            webhook("attachment_created", AttachmentCreateEvent.class),
            webhook("label_deleted", LabelDeleteEvent.class),
            webhook("label_removed", LabelRemoveEvent.class),
            webhook("label_added", LabelAddEvent.class),
            webhook("label_created", LabelCreateEvent.class),
            webhook("search_performed", SearchPerformedEvent.class),
            webhook("comment_removed", CommentRemoveEvent.class),
            webhook("comment_updated", CommentUpdateEvent.class),
            webhook("comment_created", CommentCreateEvent.class),
            webhook("login_failed", LoginFailedEvent.class),
            webhook("login", LoginEvent.class),
            webhook("logout", LogoutEvent.class),
            webhook("blog_created", BlogPostCreateEvent.class),
            webhook("blog_removed", BlogPostRemoveEvent.class),
            webhook("blog_trashed", BlogPostTrashedEvent.class),
            webhook("blog_restored", BlogPostRestoreEvent.class),
            webhook("blog_updated", BlogPostUpdateEvent.class),
            webhook("blog_viewed", BlogPostViewEvent.class),
            webhook("page_updated", PageUpdateEvent.class),
            webhook("page_created", PageCreateEvent.class),
            webhook("page_removed", PageRemoveEvent.class),
            webhook("page_trashed", PageTrashedEvent.class),
            webhook("page_restored", PageRestoreEvent.class),
            webhook("page_moved", PageMoveEvent.class),
            webhook("page_viewed", PageViewEvent.class),
            webhook("page_children_reordered", PageChildrenReorderEvent.class),
            webhook("content_permissions_updated", ContentPermissionEvent.class, new NonEmptyContentPermissionEventMatcher())
    };

    /**
     * This test checks if all webhooks that should be registered are actually registered correctly.
     * It's here to make sure the transition to the new webhooks API went well.
     */
    @Test
    public void testWebhookIsRegistered()
    {
        WebHookPluginRegistration pluginRegistration = factory.createPluginRegistration();
        assertThat(Lists.newArrayList(pluginRegistration.getRegistrations()), hasSize(expectedWebhooks.length));
        for (Webhook expectedWebhook : expectedWebhooks)
        {
            assertThat(pluginRegistration.getRegistrations(), contains(expectedWebhook));
        }
    }

    private Matcher<? super Iterable<RegisteredWebHookEvent>> contains(final Webhook webhook)
    {
        return new TypeSafeMatcher<Iterable<RegisteredWebHookEvent>>()
        {
            @Override
            protected boolean matchesSafely(final Iterable<RegisteredWebHookEvent> registeredWebHookEvents)
            {
                List<Webhook> events = Lists.newArrayList(Iterables.transform(registeredWebHookEvents, new Function<RegisteredWebHookEvent, Webhook>()
                {
                    @Override
                    public Webhook apply(final RegisteredWebHookEvent registeredWebHookEvent)
                    {
                        return new Webhook(registeredWebHookEvent.getId(), registeredWebHookEvent.getEventClass(), registeredWebHookEvent.getEventMatcher());
                    }
                }));
                return events.contains(webhook);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText(webhook.toString());
            }
        };
    }

    private static <T> Webhook<T> webhook(String id, Class<T> event)
    {
        return new Webhook<T>(id, event, EventMatchers.ALWAYS_TRUE);
    }

    private static <T> Webhook<T> webhook(String id, Class<T> event, EventMatcher<? super T> matcher)
    {
        return new Webhook<T>(id, event, matcher);
    }
}
