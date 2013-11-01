package com.atlassian.plugin.connect.plugin.product.confluence;

import java.util.Map;

import com.atlassian.core.task.MultiQueueTaskManager;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.plugin.connect.plugin.capabilities.annotation.ProductFilter;
import com.atlassian.plugin.connect.plugin.spring.ConfluenceComponent;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.util.ContextClassLoaderSwitchingUtil;
import com.atlassian.plugin.web.Condition;

import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

/**
 *
 */
@ConfluenceComponent
public final class ConfluenceProductAccessor implements ProductAccessor
{
    private static final Logger log = LoggerFactory.getLogger(ConfluenceProductAccessor.class);
    private final MultiQueueTaskManager taskManager;

    @Autowired
    public ConfluenceProductAccessor(MultiQueueTaskManager taskManager)
    {
        this.taskManager = checkNotNull(taskManager);
    }

    @Override
    public String getPreferredAdminSectionKey()
    {
        return "system.admin/marketplace_confluence";
    }

    @Override
    public int getPreferredAdminWeight()
    {
        return 100;
    }

    @Override
    public String getKey()
    {
        return "confluence";
    }

    @Override
    public int getPreferredGeneralWeight()
    {
        return 1000;
    }

    @Override
    public String getPreferredGeneralSectionKey()
    {
        return "system.browse";
    }

    @Override
    public int getPreferredProfileWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredProfileSectionKey()
    {
        return "system.profile";
    }

    @Override
    public Map<String, String> getLinkContextParams()
    {
        return ImmutableMap.of(
                "page_id", "$!page.id",
                "page_type", "$!page.type");
    }

    @Override
    public void sendEmail(String userName, final Email email, String bodyAsHtml, String bodyAsText)
    {
        // todo: support html emails for Confluence
        email.setBody(bodyAsText);
        try
        {
            ContextClassLoaderSwitchingUtil.runInContext(MailFactory.class.getClassLoader(), new Runnable()

            {
                @Override
                public void run()
                {
                    SMTPMailServer defaultSMTPMailServer = MailFactory.getServerManager()
                            .getDefaultSMTPMailServer();
                    if (defaultSMTPMailServer != null)
                    {
                        try
                        {
                            defaultSMTPMailServer.send(email);
                        }
                        catch (MailException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                    else
                    {
                        log.warn("Can't send email - no mail server defined");
                    }
                }
            });
        }
        catch (RuntimeException e)
        {
            log.warn("Unable to send email: " + email, e);
        }
    }

    @Override
    public void flushEmail()
    {
        taskManager.flush("mail");
    }

    @Override
    public Map<String, Class<? extends Condition>> getConditions()
    {
        Map<String, Class<? extends Condition>> conditions = newHashMap();
        conditions.put("not_personal_space", com.atlassian.confluence.plugin.descriptor.web.conditions.NotPersonalSpaceCondition.class);
        conditions.put("user_is_confluence_administrator", com.atlassian.confluence.plugin.descriptor.web.conditions.ConfluenceAdministratorCondition.class);
        conditions.put("user_can_use_confluence", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserCanUseConfluenceCondition.class);
        conditions.put("user_can_update_user_status", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserCanUpdateUserStatusCondition.class);
        conditions.put("not_shared_mode", com.atlassian.confluence.plugin.descriptor.web.conditions.NotSharedModeCondition.class);
        conditions.put("email_address_public", com.atlassian.confluence.plugin.descriptor.web.conditions.EmailAddressPublicCondition.class);
        conditions.put("content_has_any_permissions_set", com.atlassian.confluence.plugin.descriptor.web.conditions.ContentHasAnyPermissionsSetCondition.class);
        //conditions.put("BuildNumber", com.atlassian.confluence.plugin.descriptor.web.conditions.BuildNumberCondition.class);
        conditions.put("user_watching_space_for_content_type", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserWatchingSpaceForContentTypeCondition.class);
        conditions.put("space_function_permission", com.atlassian.confluence.plugin.descriptor.web.conditions.SpaceFunctionPermissionCondition.class);
        conditions.put("following_target_user", com.atlassian.confluence.plugin.descriptor.web.conditions.user.FollowingTargetUserCondition.class);
        conditions.put("has_space", com.atlassian.confluence.plugin.descriptor.web.conditions.HasSpaceCondition.class);
        conditions.put("target_user_can_set_status", com.atlassian.confluence.plugin.descriptor.web.conditions.user.TargetUserCanSetStatusCondition.class);
        //conditions.put("dark_feature_enabled", com.atlassian.confluence.user.DarkFeatureEnabledCondition.class);
        conditions.put("user_favouriting_target_user_personal_space", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserFavouritingTargetUserPersonalSpaceCondition.class);
        conditions.put("user_is_sysadmin", com.atlassian.confluence.plugin.descriptor.web.conditions.SystemAdministratorCondition.class);
        conditions.put("favourite_page", com.atlassian.confluence.plugin.descriptor.web.conditions.FavouritePageCondition.class);
        conditions.put("threaded_comments", com.atlassian.confluence.plugin.descriptor.web.conditions.ThreadedCommentsCondition.class);
        conditions.put("user_is_logged_in", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserLoggedInCondition.class);
        conditions.put("user_has_personal_space", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserHasPersonalSpaceCondition.class);
        conditions.put("can_signup", com.atlassian.confluence.plugin.descriptor.web.conditions.user.CanSignupCondition.class);
        conditions.put("target_user_has_personal_space", com.atlassian.confluence.plugin.descriptor.web.conditions.user.TargetUserHasPersonalSpaceCondition.class);
        conditions.put("viewing_content", com.atlassian.confluence.plugin.descriptor.web.conditions.ViewingContentCondition.class);
        conditions.put("viewing_own_profile", com.atlassian.confluence.plugin.descriptor.web.conditions.ViewingOwnProfileCondition.class);
        conditions.put("can_cluster", com.atlassian.confluence.plugin.descriptor.web.conditions.CanClusterCondition.class);
        conditions.put("active_theme", com.atlassian.confluence.plugin.descriptor.web.conditions.ActiveThemeCondition.class);
        conditions.put("printable_version", com.atlassian.confluence.plugin.descriptor.web.conditions.PrintableVersionCondition.class);
        conditions.put("writable_directory_exists", com.atlassian.confluence.plugin.descriptor.web.conditions.WritableDirectoryExistsCondition.class);
        conditions.put("user_can_create_personal_space", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserCanCreatePersonalSpaceCondition.class);
        conditions.put("tiny_url_supported", com.atlassian.confluence.plugin.descriptor.web.conditions.TinyUrlSupportedCondition.class);
        conditions.put("latest_version", com.atlassian.confluence.plugin.descriptor.web.conditions.LatestVersionCondition.class);
        conditions.put("has_page", com.atlassian.confluence.plugin.descriptor.web.conditions.HasPageCondition.class);
        conditions.put("create_content", com.atlassian.confluence.plugin.descriptor.web.conditions.CreateContentCondition.class);
        conditions.put("can_edit_space_styles", com.atlassian.confluence.plugin.descriptor.web.conditions.CanEditSpaceStylesCondition.class);
        conditions.put("showing_page_attachments", com.atlassian.confluence.plugin.descriptor.web.conditions.ShowingPageAttachmentsCondition.class);
        conditions.put("has_template", com.atlassian.confluence.plugin.descriptor.web.conditions.HasTemplateCondition.class);
        conditions.put("user_has_personal_blog", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserHasPersonalBlogCondition.class);
        conditions.put("has_attachment", com.atlassian.confluence.plugin.descriptor.web.conditions.HasAttachmentCondition.class);
        conditions.put("target_user_has_personal_blog", com.atlassian.confluence.plugin.descriptor.web.conditions.user.TargetUserHasPersonalBlogCondition.class);
        conditions.put("user_watching_space", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserWatchingSpaceCondition.class);
        conditions.put("has_blog_post", com.atlassian.confluence.plugin.descriptor.web.conditions.HasBlogPostCondition.class);
        conditions.put("user_logged_in_editable", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserLoggedInEditableCondition.class);
        conditions.put("user_watching_page", com.atlassian.confluence.plugin.descriptor.web.conditions.user.UserWatchingPageCondition.class);
        conditions.put("favourite_space", com.atlassian.confluence.plugin.descriptor.web.conditions.FavouriteSpaceCondition.class);
        return conditions;
    }
}
