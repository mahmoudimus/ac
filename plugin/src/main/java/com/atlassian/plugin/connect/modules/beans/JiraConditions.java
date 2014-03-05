package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.plugin.module.jira.conditions.ViewingOwnProfileCondition;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/*
 * NOTE: this class must be under the beans package (or a sub package) so our doclet can pick it up
 */
@JiraComponent
public class JiraConditions extends PageConditions
{
    public static final String CONDITION_LIST = getConditionListAsMarkdown(getConditionMap());
    
    public static final String HAS_SELECTED_PROJECT = "has_selected_project";
    public static final String SUB_TASKS_ENABLED = "sub_tasks_enabled";
    public static final String LINKING_ENABLED = "linking_enabled";
    public static final String TIME_TRACKING_ENABLED = "time_tracking_enabled";
    public static final String USER_HAS_ISSUE_HISTORY = "user_has_issue_history";
    public static final String USER_IS_PROJECT_ADMIN = "user_is_project_admin";
    public static final String VOTING_ENABLED = "voting_enabled";
    public static final String USER_IS_THE_LOGGED_IN_USER = "user_is_the_logged_in_user";
    public static final String WATCHING_ENABLED = "watching_enabled";
    public static final String IS_ADMIN_MODE = "is_admin_mode";
    public static final String IS_ISSUE_REPORTED_BY_CURRENT_USER = "is_issue_reported_by_current_user";
    public static final String IS_SUB_TASK = "is_sub_task";
    public static final String CAN_MANAGE_ATTACHMENTS = "can_manage_attachments";
    public static final String IS_ISSUE_EDITABLE = "is_issue_editable";
    public static final String IS_ISSUE_UNRESOLVED = "is_issue_unresolved";
    public static final String CAN_ATTACH_FILE_TO_ISSUE = "can_attach_file_to_issue";
    public static final String HAS_VOTED_FOR_ISSUE = "has_voted_for_issue";
    public static final String CAN_ATTACH_SCREENSHOT_TO_ISSUE = "can_attach_screenshot_to_issue";
    public static final String IS_ISSUE_ASSIGNED_TO_CURRENT_USER = "is_issue_assigned_to_current_user";
    public static final String IS_WATCHING_ISSUE = "is_watching_issue";
    public static final String HAS_SUB_TASKS_AVAILABLE = "has_sub_tasks_available";
    public static final String HAS_ISSUE_PERMISSION = "has_issue_permission";
    public static final String HAS_PROJECT_PERMISSION = "has_project_permission";

    public JiraConditions()
    {
        this.conditions = getConditionMap();
    }

    protected static Map<String, Class<? extends Condition>> getConditionMap()
    {
        Map<String, Class<? extends Condition>> conditionMap = PageConditions.getConditionMap();

        conditionMap.put(HAS_SELECTED_PROJECT, com.atlassian.jira.plugin.webfragment.conditions.HasSelectedProjectCondition.class);
        conditionMap.put(SUB_TASKS_ENABLED, com.atlassian.jira.plugin.webfragment.conditions.SubTasksEnabledCondition.class);
        conditionMap.put(LINKING_ENABLED, com.atlassian.jira.plugin.webfragment.conditions.LinkingEnabledCondition.class);
        conditionMap.put(TIME_TRACKING_ENABLED, com.atlassian.jira.plugin.webfragment.conditions.TimeTrackingEnabledCondition.class);
        conditionMap.put(USER_HAS_ISSUE_HISTORY, com.atlassian.jira.plugin.webfragment.conditions.UserHasIssueHistoryCondition.class);
        conditionMap.put(USER_IS_PROJECT_ADMIN, com.atlassian.jira.plugin.webfragment.conditions.UserIsProjectAdminCondition.class);
        conditionMap.put(VOTING_ENABLED, com.atlassian.jira.plugin.webfragment.conditions.VotingEnabledCondition.class);
        conditionMap.put(USER_IS_THE_LOGGED_IN_USER, ViewingOwnProfileCondition.class);
        conditionMap.put(WATCHING_ENABLED, com.atlassian.jira.plugin.webfragment.conditions.WatchingEnabledCondition.class);
        conditionMap.put(IS_ADMIN_MODE, com.atlassian.jira.plugin.webfragment.conditions.IsAdminModeCondition.class);

        // issue conditions
        conditionMap.put(IS_ISSUE_REPORTED_BY_CURRENT_USER, com.atlassian.jira.plugin.webfragment.conditions.IsIssueReportedByCurrentUserCondition.class);
        conditionMap.put(IS_SUB_TASK, com.atlassian.jira.plugin.webfragment.conditions.IsSubTaskCondition.class);
        conditionMap.put(CAN_MANAGE_ATTACHMENTS, com.atlassian.jira.plugin.webfragment.conditions.CanManageAttachmentsCondition.class);
        conditionMap.put(IS_ISSUE_EDITABLE, com.atlassian.jira.plugin.webfragment.conditions.IsIssueEditableCondition.class);
        conditionMap.put(IS_ISSUE_UNRESOLVED, com.atlassian.jira.plugin.webfragment.conditions.IsIssueUnresolvedCondition.class);
        conditionMap.put(CAN_ATTACH_FILE_TO_ISSUE, com.atlassian.jira.plugin.webfragment.conditions.CanAttachFileToIssueCondition.class);
        conditionMap.put(HAS_VOTED_FOR_ISSUE, com.atlassian.jira.plugin.webfragment.conditions.HasVotedForIssueCondition.class);
        conditionMap.put(CAN_ATTACH_SCREENSHOT_TO_ISSUE, com.atlassian.jira.plugin.webfragment.conditions.CanAttachScreenshotToIssueCondition.class);
        conditionMap.put(IS_ISSUE_ASSIGNED_TO_CURRENT_USER, com.atlassian.jira.plugin.webfragment.conditions.IsIssueAssignedToCurrentUserCondition.class);
        conditionMap.put(IS_WATCHING_ISSUE, com.atlassian.jira.plugin.webfragment.conditions.IsWatchingIssueCondition.class);
        conditionMap.put(HAS_SUB_TASKS_AVAILABLE, com.atlassian.jira.plugin.webfragment.conditions.HasSubTasksAvailableCondition.class);
        conditionMap.put(HAS_ISSUE_PERMISSION, com.atlassian.jira.plugin.webfragment.conditions.HasIssuePermissionCondition.class);
        conditionMap.put(HAS_PROJECT_PERMISSION, com.atlassian.jira.plugin.webfragment.conditions.HasProjectPermissionCondition.class);
        
        return conditionMap;
    }
}
