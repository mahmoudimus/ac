package com.atlassian.labs.remoteapps.plugin.product.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugin.webfragment.conditions.CanConvertToIssueCondition;
import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.labs.remoteapps.plugin.product.ProductAccessor;
import com.atlassian.mail.Email;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.SingleMailQueueItem;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 *
 */
public class JiraProductAccessor implements ProductAccessor
{
    private final WebInterfaceManager webInterfaceManager;
    private final UserManager userManager;
    private final MailQueue mailQueue;

    public JiraProductAccessor(WebInterfaceManager webInterfaceManager, UserManager userManager,
            MailQueue mailQueue)
    {
        this.webInterfaceManager = webInterfaceManager;
        this.userManager = userManager;
        this.mailQueue = mailQueue;
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor()
    {
        return new JiraWebItemModuleDescriptor(ComponentManager.getInstance().getJiraAuthenticationContext(), webInterfaceManager);
    }

    @Override
    public String getPreferredAdminSectionKey()
    {
        return "system.admin/system";
    }

    @Override
    public int getPreferredAdminWeight()
    {
        return 150;
    }

    @Override
    public String getKey()
    {
        return "jira";
    }

    @Override
    public int getPreferredGeneralWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredGeneralSectionKey()
    {
        return "general_dropdown_linkId/jira-remoteapps.general";
    }

    @Override
    public int getPreferredProfileWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredProfileSectionKey()
    {
        return "system.user.options/personal";
    }

    @Override
    public Map<String, String> getLinkContextParams()
    {
        return ImmutableMap.of(
                "project_id", "$!helper.project.id",
                "issue_id", "$!issue.id");
    }

    @Override
    public void sendEmail(String userName, Email email, String bodyAsHtml, String bodyAsText)
    {
        User user = userManager.getUser(userName);

        JiraUserPreferences userPrefs = new JiraUserPreferences(user);
        String prefFormat = userPrefs.getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE);

        // Default to text if the property is not configured.
        if(!"html".equalsIgnoreCase(prefFormat))
        {
            email.setMimeType("text/html");
            email.setBody(bodyAsHtml);
        }
        else
        {
            email.setMimeType("text/plain");
            email.setBody(bodyAsText);
        }
        mailQueue.addItem(new SingleMailQueueItem(email));
    }

    @Override
    public void flushEmail()
    {
        mailQueue.sendBuffer();
    }

    @Override
    public Map<String, Class<? extends Condition>> getConditions()
    {
        Map<String,Class<? extends Condition>> conditions = newHashMap();
        conditions.put("has_selected_project", com.atlassian.jira.plugin.webfragment.conditions.HasSelectedProjectCondition.class);
        conditions.put("sub_tasks_enabled", com.atlassian.jira.plugin.webfragment.conditions.SubTasksEnabledCondition.class);
        conditions.put("linking_enabled", com.atlassian.jira.plugin.webfragment.conditions.LinkingEnabledCondition.class);
        conditions.put("smtp_mail_server_configured", com.atlassian.jira.plugin.webfragment.conditions.SmtpMailServerConfiguredCondition.class);
        conditions.put("external_user_management_disabled", com.atlassian.jira.plugin.webfragment.conditions.ExternalUserManagementDisabledCondition.class);
        conditions.put("time_tracking_enabled", com.atlassian.jira.plugin.webfragment.conditions.TimeTrackingEnabledCondition.class);
        conditions.put("user_has_issue_history", com.atlassian.jira.plugin.webfragment.conditions.UserHasIssueHistoryCondition.class);
        conditions.put("user_is_project_admin", com.atlassian.jira.plugin.webfragment.conditions.UserIsProjectAdminCondition.class);
        conditions.put("is_field_hidden", com.atlassian.jira.plugin.webfragment.conditions.IsFieldHiddenCondition.class);
        conditions.put("browse_users_permission", com.atlassian.jira.plugin.webfragment.conditions.BrowseUsersPermissionCondition.class);
        conditions.put("voting_enabled", com.atlassian.jira.plugin.webfragment.conditions.VotingEnabledCondition.class);
        conditions.put("user_is_the_logged_in_user", com.atlassian.jira.plugin.webfragment.conditions.UserIsTheLoggedInUserCondition.class);
        conditions.put("has_last_search_request", com.atlassian.jira.plugin.webfragment.conditions.HasLastSearchRequestCondition.class);
        conditions.put("not_version_context", com.atlassian.jira.plugin.webfragment.conditions.NotVersionContextCondition.class);
        conditions.put("user_is_sysadmin", com.atlassian.jira.plugin.webfragment.conditions.UserIsSysAdminCondition.class);
        conditions.put("has_link_types_available", com.atlassian.jira.plugin.webfragment.conditions.HasLinkTypesAvailableCondition.class);
        conditions.put("can_create_shared_objects", com.atlassian.jira.plugin.webfragment.conditions.CanCreateSharedObjectsCondition.class);
        conditions.put("user_is_logged_in", com.atlassian.jira.plugin.webfragment.conditions.UserLoggedInCondition.class);
        conditions.put("watching_enabled", com.atlassian.jira.plugin.webfragment.conditions.WatchingEnabledCondition.class);
        conditions.put("is_keyboard_shortcuts_enabled", com.atlassian.jira.plugin.webfragment.conditions.IsKeyboardShortcutsEnabledCondition.class);
        conditions.put("user_is_admin", com.atlassian.jira.plugin.webfragment.conditions.UserIsAdminCondition.class);
        conditions.put("is_admin_mode", com.atlassian.jira.plugin.webfragment.conditions.IsAdminModeCondition.class);

        // issue conditions
        conditions.put("can_convert_to_issue", CanConvertToIssueCondition.class);
        conditions.put("is_issue_reported_by_current_user", com.atlassian.jira.plugin.webfragment.conditions.IsIssueReportedByCurrentUserCondition.class);
        conditions.put("is_sub_task", com.atlassian.jira.plugin.webfragment.conditions.IsSubTaskCondition.class);
        conditions.put("can_manage_attachments", com.atlassian.jira.plugin.webfragment.conditions.CanManageAttachmentsCondition.class);
        conditions.put("is_issue_editable", com.atlassian.jira.plugin.webfragment.conditions.IsIssueEditableCondition.class);
        conditions.put("is_issue_unresolved", com.atlassian.jira.plugin.webfragment.conditions.IsIssueUnresolvedCondition.class);
        conditions.put("can_attach_file_to_issue", com.atlassian.jira.plugin.webfragment.conditions.CanAttachFileToIssueCondition.class);
        conditions.put("has_voted_for_issue", com.atlassian.jira.plugin.webfragment.conditions.HasVotedForIssueCondition.class);
        conditions.put("can_attach_screenshot_to_issue", com.atlassian.jira.plugin.webfragment.conditions.CanAttachScreenshotToIssueCondition.class);
        conditions.put("is_issue_assigned_to_current_user", com.atlassian.jira.plugin.webfragment.conditions.IsIssueAssignedToCurrentUserCondition.class);
        conditions.put("is_watching_issue", com.atlassian.jira.plugin.webfragment.conditions.IsWatchingIssueCondition.class);
        conditions.put("has_sub_tasks_available", com.atlassian.jira.plugin.webfragment.conditions.HasSubTasksAvailableCondition.class);
        conditions.put("can_convert_to_sub_task", com.atlassian.jira.plugin.webfragment.conditions.CanConvertToSubTaskCondition.class);

        return conditions;
    }
}
