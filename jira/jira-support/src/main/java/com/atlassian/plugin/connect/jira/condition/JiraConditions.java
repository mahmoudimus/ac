package com.atlassian.plugin.connect.jira.condition;

import com.atlassian.plugin.connect.api.condition.ConnectEntityPropertyEqualToCondition;
import com.atlassian.plugin.connect.spi.condition.ConditionsProvider;
import com.atlassian.plugin.connect.spi.condition.PageConditionsFactory;
import com.atlassian.plugin.connect.spi.product.ConditionClassResolver;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.base.Predicates.not;

/*
 * NOTE: this class must be under the beans package (or a sub package) so our doclet can pick it up
 */
@JiraComponent
public class JiraConditions implements ConditionsProvider
{
    public static final String CAN_ATTACH_FILE_TO_ISSUE = "can_attach_file_to_issue";
    public static final String CAN_MANAGE_ATTACHMENTS = "can_manage_attachments";
    public static final String FEATURE_FLAG = "feature_flag";
    public static final String HAS_ISSUE_PERMISSION = "has_issue_permission";
    public static final String HAS_PROJECT_PERMISSION = "has_project_permission";
    public static final String HAS_SELECTED_PROJECT = "has_selected_project";
    public static final String HAS_SUB_TASKS_AVAILABLE = "has_sub_tasks_available";
    public static final String HAS_VOTED_FOR_ISSUE = "has_voted_for_issue";
    public static final String IS_ADMIN_MODE = "is_admin_mode";
    public static final String IS_ISSUE_ASSIGNED_TO_CURRENT_USER = "is_issue_assigned_to_current_user";
    public static final String IS_ISSUE_EDITABLE = "is_issue_editable";
    public static final String IS_ISSUE_REPORTED_BY_CURRENT_USER = "is_issue_reported_by_current_user";
    public static final String IS_ISSUE_UNRESOLVED = "is_issue_unresolved";
    public static final String IS_SUB_TASK = "is_sub_task";
    public static final String IS_WATCHING_ISSUE = "is_watching_issue";
    public static final String LINKING_ENABLED = "linking_enabled";
    public static final String SUB_TASKS_ENABLED = "sub_tasks_enabled";
    public static final String TIME_TRACKING_ENABLED = "time_tracking_enabled";
    public static final String USER_HAS_ISSUE_HISTORY = "user_has_issue_history";
    public static final String USER_IS_PROJECT_ADMIN = "user_is_project_admin";
    public static final String USER_IS_THE_LOGGED_IN_USER = "user_is_the_logged_in_user";
    public static final String VOTING_ENABLED = "voting_enabled";
    public static final String WATCHING_ENABLED = "watching_enabled";

    private final PageConditionsFactory pageConditionsFactory;

    @Autowired
    public JiraConditions(PageConditionsFactory pageConditionsFactory)
    {
        this.pageConditionsFactory = pageConditionsFactory;
    }

    @Override
    public ConditionClassResolver getConditions()
    {
        return ConditionClassResolver.builder()
                .with(pageConditionsFactory.getPageConditions())
                .mapping(FEATURE_FLAG, com.atlassian.sal.api.features.DarkFeatureEnabledCondition.class)
                .mapping(HAS_SELECTED_PROJECT, com.atlassian.jira.plugin.webfragment.conditions.HasSelectedProjectCondition.class)
                .mapping(IS_ADMIN_MODE, com.atlassian.jira.plugin.webfragment.conditions.IsAdminModeCondition.class)
                .mapping(LINKING_ENABLED, com.atlassian.jira.plugin.webfragment.conditions.LinkingEnabledCondition.class)
                .mapping(SUB_TASKS_ENABLED, com.atlassian.jira.plugin.webfragment.conditions.SubTasksEnabledCondition.class)
                .mapping(TIME_TRACKING_ENABLED, com.atlassian.jira.plugin.webfragment.conditions.TimeTrackingEnabledCondition.class)
                .mapping(USER_HAS_ISSUE_HISTORY, com.atlassian.jira.plugin.webfragment.conditions.UserHasIssueHistoryCondition.class)
                .mapping(USER_IS_PROJECT_ADMIN, com.atlassian.jira.plugin.webfragment.conditions.UserIsProjectAdminCondition.class)
                .mapping(USER_IS_THE_LOGGED_IN_USER, ViewingOwnProfileCondition.class)
                .mapping(VOTING_ENABLED, com.atlassian.jira.plugin.webfragment.conditions.VotingEnabledCondition.class)
                .mapping(WATCHING_ENABLED, com.atlassian.jira.plugin.webfragment.conditions.WatchingEnabledCondition.class)

                .rule(ConnectEntityPropertyEqualToCondition.ENTITY_PROPERTY_EQUAL_TO, not(ConnectEntityPropertyEqualToCondition.RULE_PREDICATE), com.atlassian.jira.plugin.webfragment.conditions.EntityPropertyEqualToCondition.class)

                        // issue conditions
                .mapping(CAN_ATTACH_FILE_TO_ISSUE, com.atlassian.jira.plugin.webfragment.conditions.CanAttachFileToIssueCondition.class)
                .mapping(CAN_MANAGE_ATTACHMENTS, com.atlassian.jira.plugin.webfragment.conditions.CanManageAttachmentsCondition.class)
                .mapping(HAS_ISSUE_PERMISSION, com.atlassian.jira.plugin.webfragment.conditions.HasIssuePermissionCondition.class)
                .mapping(HAS_PROJECT_PERMISSION, com.atlassian.jira.plugin.webfragment.conditions.HasProjectPermissionCondition.class)
                .mapping(HAS_SUB_TASKS_AVAILABLE, com.atlassian.jira.plugin.webfragment.conditions.HasSubTasksAvailableCondition.class)
                .mapping(HAS_VOTED_FOR_ISSUE, com.atlassian.jira.plugin.webfragment.conditions.HasVotedForIssueCondition.class)
                .mapping(IS_ISSUE_ASSIGNED_TO_CURRENT_USER, com.atlassian.jira.plugin.webfragment.conditions.IsIssueAssignedToCurrentUserCondition.class)
                .mapping(IS_ISSUE_EDITABLE, com.atlassian.jira.plugin.webfragment.conditions.IsIssueEditableCondition.class)
                .mapping(IS_ISSUE_REPORTED_BY_CURRENT_USER, com.atlassian.jira.plugin.webfragment.conditions.IsIssueReportedByCurrentUserCondition.class)
                .mapping(IS_ISSUE_UNRESOLVED, com.atlassian.jira.plugin.webfragment.conditions.IsIssueUnresolvedCondition.class)
                .mapping(IS_SUB_TASK, com.atlassian.jira.plugin.webfragment.conditions.IsSubTaskCondition.class)
                .mapping(IS_WATCHING_ISSUE, com.atlassian.jira.plugin.webfragment.conditions.IsWatchingIssueCondition.class)

                .build();
    }
}
