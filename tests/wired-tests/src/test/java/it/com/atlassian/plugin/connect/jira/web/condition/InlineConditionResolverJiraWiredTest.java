package it.com.atlassian.plugin.connect.jira.web.condition;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.fugue.Pair;
import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.bc.issue.vote.VoteService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.entity.property.EntityPropertyService.PropertyInput;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.plugin.webfragment.conditions.UserIsTheLoggedInUserCondition;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.plugin.connect.jira.web.condition.JiraConditionClassResolver;
import com.atlassian.plugin.connect.plugin.web.context.condition.InlineCondition;
import com.atlassian.plugin.connect.plugin.web.context.condition.InlineConditionResolver;
import com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver;
import com.atlassian.plugin.connect.spi.web.context.WebFragmentModuleContextExtractor;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.com.atlassian.plugin.connect.jira.util.JiraTestUtil;

import static com.atlassian.fugue.Pair.pair;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Application("jira")
@RunWith(AtlassianPluginsTestRunner.class)
public class InlineConditionResolverJiraWiredTest {
    private final static List<Pair<Pair<String, Optional<Boolean>>, Map<String, String>>> CONDITIONS = ImmutableList.<Pair<Pair<String, Optional<Boolean>>, Map<String, String>>>builder()
            .add(pair(pair("can_attach_file_to_issue", Optional.of(true)), emptyMap()))
            .add(pair(pair("can_manage_attachments", Optional.of(true)), emptyMap()))
            .add(pair(pair("can_use_application", Optional.of(true)), ImmutableMap.of("applicationKey", "jira-core")))
            .add(pair(pair("entity_property_equal_to", Optional.of(true)), ImmutableMap.of(
                    "entity", "issue",
                    "propertyKey", "test",
                    "value", "testValue")))
            .add(pair(pair("has_issue_permission", Optional.of(true)), ImmutableMap.of("permission", Permissions.getShortName(Permissions.EDIT_ISSUE))))
            .add(pair(pair("has_issue_permission", Optional.of(true)), ImmutableMap.of("permission", ProjectPermissions.EDIT_ISSUES.permissionKey())))
            .add(pair(pair("has_project_permission", Optional.of(true)), ImmutableMap.of("permission", Permissions.getShortName(Permissions.PROJECT_ADMIN))))
            .add(pair(pair("has_project_permission", Optional.of(true)), ImmutableMap.of("permission", ProjectPermissions.EDIT_ISSUES.permissionKey())))
            .add(pair(pair("has_global_permission", Optional.of(true)), ImmutableMap.of("permission", GlobalPermissionKey.ADMINISTER.getKey())))
            .add(pair(pair("has_selected_project", Optional.of(true)), emptyMap()))
            .add(pair(pair("has_sub_tasks_available", Optional.of(true)), emptyMap()))
            .add(pair(pair("has_voted_for_issue", Optional.of(true)), emptyMap()))
            .add(pair(pair("is_admin_mode", Optional.of(true)), emptyMap()))
            .add(pair(pair("is_issue_assigned_to_current_user", Optional.of(true)), emptyMap()))
            .add(pair(pair("is_issue_editable", Optional.of(true)), emptyMap()))
            .add(pair(pair("is_issue_reported_by_current_user", Optional.of(true)), emptyMap()))
            .add(pair(pair("is_issue_unresolved", Optional.of(true)), emptyMap()))
            .add(pair(pair("is_sub_task", Optional.of(true)), emptyMap()))
            .add(pair(pair("is_watching_issue", Optional.of(true)), emptyMap()))
            .add(pair(pair("linking_enabled", Optional.of(true)), emptyMap()))
            .add(pair(pair("sub_tasks_enabled", Optional.of(true)), emptyMap()))
            .add(pair(pair("time_tracking_enabled", Optional.of(true)), emptyMap()))
            .add(pair(pair("user_has_issue_history", Optional.of(true)), emptyMap()))
            .add(pair(pair("user_is_project_admin", Optional.of(true)), emptyMap()))
            .add(pair(pair("user_is_the_logged_in_user", Optional.empty()), emptyMap()))
            .add(pair(pair("voting_enabled", Optional.of(true)), emptyMap()))
            .add(pair(pair("watching_enabled", Optional.of(true)), Collections.<String, String>emptyMap()))
            .build();

    private final InlineConditionResolver inlineConditionResolver;
    private final WebFragmentModuleContextExtractor extractor;
    private final TestAuthenticator testAuthenticator;
    private final JiraTestUtil jiraTestUtil;
    private final VoteService voteService;
    private final SubTaskManager subTaskManager;
    private final UserIssueHistoryManager historyManager;
    private final IssuePropertyService issuePropertyService;
    private final JiraConditionClassResolver conditionClassResolver = new JiraConditionClassResolver();

    private final HttpServletRequest httpRequest = mock(HttpServletRequest.class);

    private Issue issue;
    private Project project;

    public InlineConditionResolverJiraWiredTest(
            final InlineConditionResolver inlineConditionResolver,
            final WebFragmentModuleContextExtractor extractor,
            final TestAuthenticator testAuthenticator,
            final JiraTestUtil jiraTestUtil,
            final VoteService voteService,
            final SubTaskManager subTaskManager,
            final UserIssueHistoryManager historyManager,
            final IssuePropertyService issuePropertyService) {
        this.inlineConditionResolver = inlineConditionResolver;
        this.extractor = extractor;
        this.testAuthenticator = testAuthenticator;
        this.jiraTestUtil = jiraTestUtil;
        this.voteService = voteService;
        this.subTaskManager = subTaskManager;
        this.historyManager = historyManager;
        this.issuePropertyService = issuePropertyService;
    }

    @Before
    public void setUp() throws Exception {
        final ApplicationUser user = jiraTestUtil.getAdmin();
        testAuthenticator.authenticateUser(user.getUsername());

        issue = jiraTestUtil.createIssue();
        project = issue.getProjectObject();

        // set up state so that all conditions pass

        subTaskManager.enableSubTasks();
        subTaskManager.createSubTaskIssueLink(jiraTestUtil.createIssue(), issue, user);
        voteService.addVote(user, voteService.validateAddVote(user, user, issue));
        when(httpRequest.getAttribute("jira.admin.mode")).thenReturn(true);
        when(httpRequest.getAttribute(UserIsTheLoggedInUserCondition.PROFILE_USER)).thenReturn(user);
        historyManager.addIssueToHistory(user, issue);
        issuePropertyService.setProperty(user, issuePropertyService.validateSetProperty(user, issue.getId(), new PropertyInput("\"testValue\"", "test")));
    }

    @Test
    public void testAllConditionsAreTested() throws Exception {
        Set<String> allConditions = conditionClassResolver.getEntries().stream()
                .map(ConnectConditionClassResolver.Entry::getConditionName)
                .collect(toSet());

        Set<String> testedConditions = CONDITIONS.stream()
                .map(pair -> pair.left().left())
                .collect(toSet());

        Sets.SetView<String> untestedConditions = Sets.difference(allConditions, testedConditions);
        assertTrue("All conditions should be tested, untested conditions: " + untestedConditions, untestedConditions.isEmpty());
    }

    @Test
    public void testConditions() throws Exception {
        Map<String, Object> reversedContext = extractor.reverseExtraction(httpRequest, redirectContext());

        for (Pair<Pair<String, Optional<Boolean>>, Map<String, String>> conditionAndParams : CONDITIONS) {
            String name = conditionAndParams.left().left();
            Optional<Boolean> expectedValue = conditionAndParams.left().right();
            Map<String, String> params = conditionAndParams.right();
            InlineCondition condition = new InlineCondition(name, params);
            Optional<Boolean> resolved = inlineConditionResolver.resolve(condition, reversedContext);
            assertEquals(pair(name, expectedValue), pair(name, resolved));
        }
    }

    private Map<String, String> redirectContext() {
        return ImmutableMap.of(
                "issue.id", issue.getId().toString(),
                "project.id", project.getId().toString());
    }
}

