package it.jira.condition;

import java.util.List;

import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.testkit.client.IssuesControl;
import com.atlassian.jira.testkit.client.restclient.VotesClient;
import com.atlassian.jira.testkit.client.restclient.WatchersClient;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import com.google.common.collect.ImmutableList;
import it.jira.JiraWebDriverTestBase;

import static it.jira.condition.TestedCondition.condition;

public abstract class AbstractJiraConditionsTest extends JiraWebDriverTestBase
{
    protected static final List<TestedCondition> CONDITIONS = ImmutableList.<TestedCondition>builder()
            .add(condition("has_selected_project"))
            .add(condition("linking_enabled"))
            .add(condition("sub_tasks_enabled"))
            .add(condition("time_tracking_enabled"))
            .add(condition("user_is_project_admin"))
            .add(condition("voting_enabled"))
            .add(condition("watching_enabled"))
            .add(condition("can_attach_file_to_issue"))
            .add(condition("can_manage_attachments"))
            .add(condition("has_issue_permission").withParam("permission", ProjectPermissions.EDIT_ISSUES.permissionKey()))
            .add(condition("has_issue_permission").withParam("permission", Permissions.getShortName(Permissions.EDIT_ISSUE))) // legacy behaviour
            .add(condition("has_project_permission").withParam("permission", ProjectPermissions.ADMINISTER_PROJECTS.permissionKey()))
            .add(condition("has_project_permission").withParam("permission", Permissions.getShortName(Permissions.PROJECT_ADMIN))) // legacy behaviour
            .add(condition("has_global_permission").withParam("permission", GlobalPermissionKey.ADMINISTER.getKey()))
            .add(condition("has_sub_tasks_available"))
            .add(condition("has_voted_for_issue"))
            .add(condition("is_issue_assigned_to_current_user"))
            .add(condition("is_issue_editable"))
            .add(condition("is_issue_unresolved"))
            .add(condition("is_sub_task"))
            .add(condition("is_watching_issue"))
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
