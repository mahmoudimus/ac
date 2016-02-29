package com.atlassian.plugin.connect.jira.web.condition;

import com.atlassian.jira.plugin.webfragment.conditions.JiraGlobalPermissionCondition;
import com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver.Entry.newEntry;

public class JiraConditionClassResolver implements ConnectConditionClassResolver {

    @Override
    @SuppressWarnings("unchecked")
    public List<Entry> getEntries() {
        return ImmutableList.of(
                newEntry("has_selected_project", com.atlassian.jira.plugin.webfragment.conditions.HasSelectedProjectCondition.class).build(),
                newEntry("is_admin_mode", com.atlassian.jira.plugin.webfragment.conditions.IsAdminModeCondition.class).build(),
                newEntry("linking_enabled", com.atlassian.jira.plugin.webfragment.conditions.LinkingEnabledCondition.class).build(),
                newEntry("sub_tasks_enabled", com.atlassian.jira.plugin.webfragment.conditions.SubTasksEnabledCondition.class).build(),
                newEntry("time_tracking_enabled", com.atlassian.jira.plugin.webfragment.conditions.TimeTrackingEnabledCondition.class).build(),
                newEntry("user_has_issue_history", com.atlassian.jira.plugin.webfragment.conditions.UserHasIssueHistoryCondition.class).build(),
                newEntry("user_is_project_admin", com.atlassian.jira.plugin.webfragment.conditions.UserIsProjectAdminCondition.class).build(),
                newEntry("user_is_the_logged_in_user", ViewingOwnProfileCondition.class).withoutInlineSupport().build(),
                newEntry("voting_enabled", com.atlassian.jira.plugin.webfragment.conditions.VotingEnabledCondition.class).build(),
                newEntry("watching_enabled", com.atlassian.jira.plugin.webfragment.conditions.WatchingEnabledCondition.class).build(),
                newEntry("can_use_application", CanUseApplicationCondition.class).contextFree().build(),

                newEntry("entity_property_equal_to", com.atlassian.jira.plugin.webfragment.conditions.EntityPropertyEqualToCondition.class)
                        .withPredicates(new Predicate<Map<String, String>>() {
                            @Override
                            public boolean test(Map<String, String> parameters) {
                                return !"addon".equals(parameters.get("entity"));
                            }
                        })
                        .build(),

                // issue conditions
                newEntry("can_attach_file_to_issue", com.atlassian.jira.plugin.webfragment.conditions.CanAttachFileToIssueCondition.class).build(),
                newEntry("can_manage_attachments", com.atlassian.jira.plugin.webfragment.conditions.CanManageAttachmentsCondition.class).build(),
                newEntry("has_issue_permission", com.atlassian.jira.plugin.webfragment.conditions.HasIssuePermissionCondition.class).build(),
                newEntry("has_project_permission", com.atlassian.jira.plugin.webfragment.conditions.HasProjectPermissionCondition.class).build(),
                newEntry("has_global_permission", JiraGlobalPermissionCondition.class).build(),
                newEntry("has_sub_tasks_available", com.atlassian.jira.plugin.webfragment.conditions.HasSubTasksAvailableCondition.class).build(),
                newEntry("has_voted_for_issue", com.atlassian.jira.plugin.webfragment.conditions.HasVotedForIssueCondition.class).build(),
                newEntry("is_issue_assigned_to_current_user", com.atlassian.jira.plugin.webfragment.conditions.IsIssueAssignedToCurrentUserCondition.class).build(),
                newEntry("is_issue_editable", com.atlassian.jira.plugin.webfragment.conditions.IsIssueEditableCondition.class).build(),
                newEntry("is_issue_reported_by_current_user", com.atlassian.jira.plugin.webfragment.conditions.IsIssueReportedByCurrentUserCondition.class).build(),
                newEntry("is_issue_unresolved", com.atlassian.jira.plugin.webfragment.conditions.IsIssueUnresolvedCondition.class).build(),
                newEntry("is_sub_task", com.atlassian.jira.plugin.webfragment.conditions.IsSubTaskCondition.class).build(),
                newEntry("is_watching_issue", com.atlassian.jira.plugin.webfragment.conditions.IsWatchingIssueCondition.class).build()
        );
    }
}
