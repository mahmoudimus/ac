package it.jira.condition;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.fugue.Pair;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.testkit.client.IssuesControl;
import com.atlassian.jira.testkit.client.restclient.VotesClient;
import com.atlassian.jira.testkit.client.restclient.WatchersClient;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.jira.JiraWebDriverTestBase;

import static com.atlassian.fugue.Pair.pair;

public abstract class AbstractJiraConditionsTest extends JiraWebDriverTestBase
{
    protected static final List<Pair<String, Map<String, String>>> CONDITION_NAMES = ImmutableList.<Pair<String, Map<String, String>>>builder()
            .add(pair("has_selected_project", Collections.emptyMap()))
            .add(pair("linking_enabled", Collections.emptyMap()))
            .add(pair("sub_tasks_enabled", Collections.emptyMap()))
            .add(pair("time_tracking_enabled", Collections.emptyMap()))
            .add(pair("user_is_project_admin", Collections.emptyMap()))
            .add(pair("voting_enabled", Collections.emptyMap()))
            .add(pair("watching_enabled", Collections.emptyMap()))
            .add(pair("can_attach_file_to_issue", Collections.emptyMap()))
            .add(pair("can_manage_attachments", Collections.emptyMap()))
            .add(pair("has_issue_permission", ImmutableMap.of("permission", ProjectPermissions.EDIT_ISSUES.permissionKey())))
            .add(pair("has_issue_permission", ImmutableMap.of("permission", Permissions.getShortName(Permissions.EDIT_ISSUE)))) // legacy behaviour
            .add(pair("has_project_permission", ImmutableMap.of("permission", ProjectPermissions.ADMINISTER_PROJECTS.permissionKey())))
            .add(pair("has_project_permission", ImmutableMap.of("permission", Permissions.getShortName(Permissions.PROJECT_ADMIN)))) // legacy behaviour
            .add(pair("has_global_permission", ImmutableMap.of("permission", GlobalPermissionKey.ADMINISTER.getKey())))
            .add(pair("has_sub_tasks_available", Collections.emptyMap()))
            .add(pair("has_voted_for_issue", Collections.emptyMap()))
            .add(pair("is_issue_assigned_to_current_user", Collections.emptyMap()))
            .add(pair("is_issue_editable", Collections.emptyMap()))
            .add(pair("is_issue_unresolved", Collections.emptyMap()))
            .add(pair("is_sub_task", Collections.emptyMap()))
            .add(pair("is_watching_issue", Collections.emptyMap()))
            .build();

    protected final String createIssueSatisfyingAllConditions(TestUser user)
    {
        IssuesControl issuesControl = product.backdoor().issues();
        IssueCreateResponse issue = issuesControl.createIssue(project.getKey(), "Test Issue");
        IssueCreateResponse subTask = issuesControl.createSubtask(project.getId(), issue.key, "Test Sub-Task");
        String issueKey = subTask.key;
        issuesControl.assignIssue(issueKey, user.getUsername());
        watchIssue(user, issueKey);
        voteIssue(user, issueKey);
        return issueKey;
    }

    protected final void watchIssue(TestUser user, String issueKey)
    {
        WatchersClient watchersClient = new WatchersClient(product.environmentData());
        watchersClient.postResponse(issueKey, user.getUsername());
    }

    protected final void voteIssue(TestUser user, String issueKey)
    {
        VotesClient votesClient = new VotesClient(product.environmentData());
        votesClient.loginAs(user.getUsername(), user.getPassword());
        votesClient.postResponse(issueKey);
    }
}
