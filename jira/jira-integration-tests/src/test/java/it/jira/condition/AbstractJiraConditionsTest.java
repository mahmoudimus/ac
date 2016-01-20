package it.jira.condition;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.testkit.client.IssuesControl;
import com.atlassian.jira.testkit.client.restclient.VotesClient;
import com.atlassian.jira.testkit.client.restclient.WatchersClient;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import com.google.common.collect.ImmutableMap;
import it.jira.JiraWebDriverTestBase;
import org.junit.Ignore;

import static com.google.common.collect.Lists.newArrayList;

@Ignore
public abstract class AbstractJiraConditionsTest extends JiraWebDriverTestBase
{
    protected static final List<String> CONDITION_NAMES = newArrayList(
            "has_selected_project",
            "linking_enabled",
            "sub_tasks_enabled",
            "time_tracking_enabled",
            "user_is_project_admin",
            "voting_enabled",
            "watching_enabled",
            "can_attach_file_to_issue",
            "can_manage_attachments",
            "has_issue_permission",
            "has_project_permission",
            "has_sub_tasks_available",
            "has_voted_for_issue",
            "is_issue_assigned_to_current_user",
            "is_issue_editable",
            "is_issue_unresolved",
            "is_sub_task",
            "is_watching_issue"
    );

    protected static final Map<String, Map<String, String>> CONDITION_PARAMETERS = ImmutableMap.of(
            "has_issue_permission", ImmutableMap.of("permission", Permissions.getShortName(Permissions.EDIT_ISSUE)),
            "has_project_permission", ImmutableMap.of("permission", Permissions.getShortName(Permissions.PROJECT_ADMIN))
    );

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
