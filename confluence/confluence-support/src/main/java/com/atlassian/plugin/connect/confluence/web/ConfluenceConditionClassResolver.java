package com.atlassian.plugin.connect.confluence.web;

import com.atlassian.plugin.connect.api.web.condition.UserIsAdminCondition;
import com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver.Entry.newEntry;

public class ConfluenceConditionClassResolver implements ConnectConditionClassResolver
{

    public static final String SPACE_SIDEBAR = "space_sidebar";

    @Override
    public List<Entry> getEntries()
    {
        return ImmutableList.of(
                newEntry("active_theme", com.atlassian.confluence.plugin.descriptor.web.conditions.ActiveThemeCondition.class).build(),
                newEntry("can_edit_space_styles", com.atlassian.confluence.plugin.descriptor.web.conditions.CanEditSpaceStylesCondition.class).build(),
                newEntry("can_signup", com.atlassian.confluence.plugin.descriptor.web.conditions.user.CanSignupCondition.class).build(),
                newEntry("content_has_any_permissions_set", com.atlassian.confluence.plugin.descriptor.web.conditions.ContentHasAnyPermissionsSetCondition.class).build(),
                newEntry("create_content", com.atlassian.confluence.plugin.descriptor.web.conditions.CreateContentCondition.class).build(),
                newEntry("email_address_public", com.atlassian.confluence.plugin.descriptor.web.conditions.EmailAddressPublicCondition.class).build(),
                newEntry("favourite_page", com.atlassian.confluence.plugin.descriptor.web.conditions.FavouritePageCondition.class).build(),
                newEntry("favourite_space", com.atlassian.confluence.plugin.descriptor.web.conditions.FavouriteSpaceCondition.class).build(),
                newEntry("following_target_user", com.atlassian.confluence.plugin.descriptor.web.conditions.user.FollowingTargetUserCondition.class).build(),
                newEntry("has_attachment", com.atlassian.confluence.plugin.descriptor.web.conditions.HasAttachmentCondition.class).build(),
                newEntry("has_blog_post", com.atlassian.confluence.plugin.descriptor.web.conditions.HasBlogPostCondition.class).build(),
                newEntry("has_page", com.atlassian.confluence.plugin.descriptor.web.conditions.HasPageCondition.class).build(),
                newEntry("has_space", com.atlassian.confluence.plugin.descriptor.web.conditions.HasSpaceCondition.class).build(),
                newEntry("has_template", com.atlassian.confluence.plugin.descriptor.web.conditions.HasTemplateCondition.class).build(),
                newEntry("latest_version", com.atlassian.confluence.plugin.descriptor.web.conditions.LatestVersionCondition.class).build(),
                newEntry("not_personal_space", com.atlassian.confluence.plugin.descriptor.web.conditions.NotPersonalSpaceCondition.class).build(),
                newEntry("printable_version", com.atlassian.confluence.plugin.descriptor.web.conditions.PrintableVersionCondition.class).build(),
                newEntry("showing_page_attachments", com.atlassian.confluence.plugin.descriptor.web.conditions.ShowingPageAttachmentsCondition.class).build(),
                newEntry("space_function_permission", com.atlassian.confluence.plugin.descriptor.web.conditions.SpaceFunctionPermissionCondition.class).build(),
                newEntry(SPACE_SIDEBAR, com.atlassian.confluence.plugin.descriptor.web.conditions.SpaceSidebarCondition.class).build(),
                newEntry("target_user_has_personal_blog", com.atlassian.confluence.plugin.descriptor.web.conditions.user.TargetUserHasPersonalBlogCondition.class).build(),
                newEntry("target_user_has_personal_space", com.atlassian.confluence.plugin.descriptor.web.conditions.user.TargetUserHasPersonalSpaceCondition.class).build(),
                newEntry("threaded_comments", com.atlassian.confluence.plugin.descriptor.web.conditions.ThreadedCommentsCondition.class).build(),
                newEntry("tiny_url_supported", com.atlassian.confluence.plugin.descriptor.web.conditions.TinyUrlSupportedCondition.class).build(),
                newEntry("user_can_create_personal_space", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserCanCreatePersonalSpaceCondition.class).build(),
                newEntry("user_can_use_confluence", com.atlassian.confluence.plugin.descriptor.web.conditions.user.TargetUserCanUseConfluenceCondition.class).build(),
                newEntry("user_favouriting_target_user_personal_space", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserFavouritingTargetUserPersonalSpaceCondition.class).build(),
                newEntry("user_has_personal_blog", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserHasPersonalBlogCondition.class).build(),
                newEntry("user_has_personal_space", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserHasPersonalSpaceCondition.class).build(),
                newEntry("user_logged_in_editable", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserLoggedInEditableCondition.class).build(),
                newEntry("user_watching_page", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserWatchingPageCondition.class).build(),
                newEntry("user_watching_space", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserWatchingSpaceCondition.class).build(),
                newEntry("user_watching_space_for_content_type", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserWatchingSpaceForContentTypeCondition.class).build(),
                newEntry("viewing_content", com.atlassian.confluence.plugin.descriptor.web.conditions.ViewingContentCondition.class).build(),
                newEntry("viewing_own_profile", com.atlassian.confluence.plugin.descriptor.web.conditions.ViewingOwnProfileCondition.class).build(),

                //just here for backwards compatibility.
                newEntry("user_is_confluence_administrator", UserIsAdminCondition.class).build()
        );
    }
}
