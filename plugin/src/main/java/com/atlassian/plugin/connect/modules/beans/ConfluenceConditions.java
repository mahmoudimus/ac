package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.spi.condition.UserIsAdminCondition;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/*
 * NOTE: this class must be under the beans package (or a sub package) so our doclet can pick it up
 */
@ConfluenceComponent
public class ConfluenceConditions extends PageConditions
{
    
    public static final String CONDITION_LIST = getConditionListAsMarkdown(getConditionMap());
    public static final String NOT_PERSONAL_SPACE = "not_personal_space";
    public static final String USER_CAN_USE_CONFLUENCE = "user_can_use_confluence";
    public static final String USER_CAN_UPDATE_USER_STATUS = "user_can_update_user_status";
    public static final String EMAIL_ADDRESS_PUBLIC = "email_address_public";
    public static final String CONTENT_HAS_ANY_PERMISSIONS_SET = "content_has_any_permissions_set";
    public static final String USER_WATCHING_SPACE_FOR_CONTENT_TYPE = "user_watching_space_for_content_type";
    public static final String SPACE_FUNCTION_PERMISSION = "space_function_permission";
    public static final String FOLLOWING_TARGET_USER = "following_target_user";
    public static final String HAS_SPACE = "has_space";
    public static final String TARGET_USER_CAN_SET_STATUS = "target_user_can_set_status";
    public static final String USER_FAVOURITING_TARGET_USER_PERSONAL_SPACE = "user_favouriting_target_user_personal_space";
    public static final String FAVOURITE_PAGE = "favourite_page";
    public static final String THREADED_COMMENTS = "threaded_comments";
    public static final String USER_HAS_PERSONAL_SPACE = "user_has_personal_space";
    public static final String CAN_SIGNUP = "can_signup";
    public static final String TARGET_USER_HAS_PERSONAL_SPACE = "target_user_has_personal_space";
    public static final String VIEWING_CONTENT = "viewing_content";
    public static final String VIEWING_OWN_PROFILE = "viewing_own_profile";
    public static final String ACTIVE_THEME = "active_theme";
    public static final String PRINTABLE_VERSION = "printable_version";
    public static final String USER_CAN_CREATE_PERSONAL_SPACE = "user_can_create_personal_space";
    public static final String TINY_URL_SUPPORTED = "tiny_url_supported";
    public static final String LATEST_VERSION = "latest_version";
    public static final String HAS_PAGE = "has_page";
    public static final String CREATE_CONTENT = "create_content";
    public static final String CAN_EDIT_SPACE_STYLES = "can_edit_space_styles";
    public static final String SHOWING_PAGE_ATTACHMENTS = "showing_page_attachments";
    public static final String HAS_TEMPLATE = "has_template";
    public static final String USER_HAS_PERSONAL_BLOG = "user_has_personal_blog";
    public static final String HAS_ATTACHMENT = "has_attachment";
    public static final String TARGET_USER_HAS_PERSONAL_BLOG = "target_user_has_personal_blog";
    public static final String USER_WATCHING_SPACE = "user_watching_space";
    public static final String HAS_BLOG_POST = "has_blog_post";
    public static final String USER_LOGGED_IN_EDITABLE = "user_logged_in_editable";
    public static final String USER_WATCHING_PAGE = "user_watching_page";
    public static final String FAVOURITE_SPACE = "favourite_space";
    public static final String SPACE_SIDEBAR = "space_sidebar";
    
    public static final String USER_IS_CONFLUENCE_ADMIN = "user_is_confluence_administrator";

    public ConfluenceConditions()
    {
        this.conditions = getConditionMap();
    }

    protected static Map<String, Class<? extends Condition>> getConditionMap()
    {
        Map<String, Class<? extends Condition>> conditionMap = PageConditions.getConditionMap();

        conditionMap.put(ACTIVE_THEME, com.atlassian.confluence.plugin.descriptor.web.conditions.ActiveThemeCondition.class);
        conditionMap.put(CAN_EDIT_SPACE_STYLES, com.atlassian.confluence.plugin.descriptor.web.conditions.CanEditSpaceStylesCondition.class);
        conditionMap.put(CAN_SIGNUP, com.atlassian.confluence.plugin.descriptor.web.conditions.user.CanSignupCondition.class);
        conditionMap.put(CONTENT_HAS_ANY_PERMISSIONS_SET, com.atlassian.confluence.plugin.descriptor.web.conditions.ContentHasAnyPermissionsSetCondition.class);
        conditionMap.put(CREATE_CONTENT, com.atlassian.confluence.plugin.descriptor.web.conditions.CreateContentCondition.class);
        conditionMap.put(EMAIL_ADDRESS_PUBLIC, com.atlassian.confluence.plugin.descriptor.web.conditions.EmailAddressPublicCondition.class);
        conditionMap.put(FAVOURITE_PAGE, com.atlassian.confluence.plugin.descriptor.web.conditions.FavouritePageCondition.class);
        conditionMap.put(FAVOURITE_SPACE, com.atlassian.confluence.plugin.descriptor.web.conditions.FavouriteSpaceCondition.class);
        conditionMap.put(FOLLOWING_TARGET_USER, com.atlassian.confluence.plugin.descriptor.web.conditions.user.FollowingTargetUserCondition.class);
        conditionMap.put(HAS_ATTACHMENT, com.atlassian.confluence.plugin.descriptor.web.conditions.HasAttachmentCondition.class);
        conditionMap.put(HAS_BLOG_POST, com.atlassian.confluence.plugin.descriptor.web.conditions.HasBlogPostCondition.class);
        conditionMap.put(HAS_PAGE, com.atlassian.confluence.plugin.descriptor.web.conditions.HasPageCondition.class);
        conditionMap.put(HAS_SPACE, com.atlassian.confluence.plugin.descriptor.web.conditions.HasSpaceCondition.class);
        conditionMap.put(HAS_TEMPLATE, com.atlassian.confluence.plugin.descriptor.web.conditions.HasTemplateCondition.class);
        conditionMap.put(LATEST_VERSION, com.atlassian.confluence.plugin.descriptor.web.conditions.LatestVersionCondition.class);
        conditionMap.put(NOT_PERSONAL_SPACE, com.atlassian.confluence.plugin.descriptor.web.conditions.NotPersonalSpaceCondition.class);
        conditionMap.put(PRINTABLE_VERSION, com.atlassian.confluence.plugin.descriptor.web.conditions.PrintableVersionCondition.class);
        conditionMap.put(SHOWING_PAGE_ATTACHMENTS, com.atlassian.confluence.plugin.descriptor.web.conditions.ShowingPageAttachmentsCondition.class);
        conditionMap.put(SPACE_FUNCTION_PERMISSION, com.atlassian.confluence.plugin.descriptor.web.conditions.SpaceFunctionPermissionCondition.class);
        conditionMap.put(SPACE_SIDEBAR, com.atlassian.confluence.plugin.descriptor.web.conditions.SpaceSidebarCondition.class);
        conditionMap.put(TARGET_USER_CAN_SET_STATUS, com.atlassian.confluence.plugin.descriptor.web.conditions.user.TargetUserCanSetStatusCondition.class);
        conditionMap.put(TARGET_USER_HAS_PERSONAL_BLOG, com.atlassian.confluence.plugin.descriptor.web.conditions.user.TargetUserHasPersonalBlogCondition.class);
        conditionMap.put(TARGET_USER_HAS_PERSONAL_SPACE, com.atlassian.confluence.plugin.descriptor.web.conditions.user.TargetUserHasPersonalSpaceCondition.class);
        conditionMap.put(THREADED_COMMENTS, com.atlassian.confluence.plugin.descriptor.web.conditions.ThreadedCommentsCondition.class);
        conditionMap.put(TINY_URL_SUPPORTED, com.atlassian.confluence.plugin.descriptor.web.conditions.TinyUrlSupportedCondition.class);
        conditionMap.put(USER_CAN_CREATE_PERSONAL_SPACE, com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserCanCreatePersonalSpaceCondition.class);
        conditionMap.put(USER_CAN_UPDATE_USER_STATUS, com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserCanUpdateUserStatusCondition.class);
        conditionMap.put(USER_CAN_USE_CONFLUENCE, com.atlassian.confluence.plugin.descriptor.web.conditions.user.TargetUserCanUseConfluenceCondition.class);
        conditionMap.put(USER_FAVOURITING_TARGET_USER_PERSONAL_SPACE, com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserFavouritingTargetUserPersonalSpaceCondition.class);
        conditionMap.put(USER_HAS_PERSONAL_BLOG, com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserHasPersonalBlogCondition.class);
        conditionMap.put(USER_HAS_PERSONAL_SPACE, com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserHasPersonalSpaceCondition.class);
        conditionMap.put(USER_LOGGED_IN_EDITABLE, com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserLoggedInEditableCondition.class);
        conditionMap.put(USER_WATCHING_PAGE, com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserWatchingPageCondition.class);
        conditionMap.put(USER_WATCHING_SPACE, com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserWatchingSpaceCondition.class);
        conditionMap.put(USER_WATCHING_SPACE_FOR_CONTENT_TYPE, com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserWatchingSpaceForContentTypeCondition.class);
        conditionMap.put(VIEWING_CONTENT, com.atlassian.confluence.plugin.descriptor.web.conditions.ViewingContentCondition.class);
        conditionMap.put(VIEWING_OWN_PROFILE, com.atlassian.confluence.plugin.descriptor.web.conditions.ViewingOwnProfileCondition.class);
        
        //just here for backwards compatibility.
        conditionMap.put(USER_IS_CONFLUENCE_ADMIN, UserIsAdminCondition.class);

        return conditionMap;
    }

    
}
