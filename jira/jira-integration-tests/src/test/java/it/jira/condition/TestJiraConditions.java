package it.jira.condition;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.atlassian.jira.pageobjects.pages.viewissue.IssueMenu;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.testkit.client.IssuesControl;
import com.atlassian.jira.testkit.client.restclient.VotesClient;
import com.atlassian.jira.testkit.client.restclient.WatchersClient;
import com.atlassian.plugin.connect.jira.web.condition.JiraConditionClassResolver;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.util.TestUser;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import it.jira.JiraWebDriverTestBase;
import it.jira.item.TestJiraWebItemWithProductCondition;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests class resolution, autowiring and evaluation of most conditions in {@link JiraConditionClassResolver}.
 *
 * Conditions covered elsewhere:
 * - is_admin_mode - {@link TestJiraWebItemWithProductCondition#shouldPerformActionForWebItemWithAdminModeCondition()}
 * - entity_property_equal_to - {@link TestEntityPropertyEqualToCondition}
 *
 * Not covered:
 * - user_is_the_logged_in_user
 * - user_has_issue_history
 * - is_issue_reported_by_current_user
 */
public class TestJiraConditions extends JiraWebDriverTestBase
{

    private static final List<String> CONDITION_NAMES = newArrayList(
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

    private static final Map<String, Map<String, String>> CONDITION_PARAMETERS = ImmutableMap.of(
            "has_issue_permission", ImmutableMap.of("permission", Permissions.getShortName(Permissions.EDIT_ISSUE)),
            "has_project_permission", ImmutableMap.of("permission", Permissions.getShortName(Permissions.PROJECT_ADMIN))
    );

    private static ConnectRunner addon;

    @BeforeClass
    public static void startAddon() throws Exception
    {
        addon = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone();
        addWebItemsWithConditions();
        addon.start();
    }

    @AfterClass
    public static void stopAddon() throws Exception
    {
        if (addon != null)
        {
            addon.stopAndUninstall();
        }
    }

    @Test
    public void shouldDisplayWebItemsWithEachCondition()
    {
        TestUser admin = testUserFactory.admin();
        login(admin);

        String issueKey = createIssueSatisfyingAllConditions(admin);

        ViewIssuePage viewIssuePage = product.visit(ViewIssuePage.class, issueKey);
        IssueMenu issueMenu = viewIssuePage.getIssueMenu();
        issueMenu.openMoreActions();

        List<String> passedConditions = CONDITION_NAMES.stream()
                .filter((conditionName) -> isItemPresentInMoreActionsMenu(issueMenu, getDisplayNameForCondition(conditionName)))
                .collect(Collectors.toList());

        assertThat(passedConditions, equalTo(CONDITION_NAMES));
    }

    private static void addWebItemsWithConditions()
    {
        for (String conditionName : CONDITION_NAMES)
        {
            addon.addModules("webItems", newWebItemBeanWithCondition(conditionName));
        }
    }

    private static WebItemModuleBean newWebItemBeanWithCondition(String conditionName)
    {
        SingleConditionBeanBuilder conditionBeanBuilder = newSingleConditionBean().withCondition(conditionName);
        if (CONDITION_PARAMETERS.containsKey(conditionName))
        {
            conditionBeanBuilder.withParams(CONDITION_PARAMETERS.get(conditionName));
        }
        return newWebItemBean()
                .withKey(conditionName.replace('_', '-'))
                .withUrl("/path-without-route")
                .withLocation("operations-work")
                .withWeight(CONDITION_NAMES.indexOf(conditionName))
                .withName(new I18nProperty(getDisplayNameForCondition(conditionName), null))
                .withConditions(conditionBeanBuilder.build())
                .build();
    }

    private static String getDisplayNameForCondition(String conditionName)
    {
        return String.format("%d %s", CONDITION_NAMES.indexOf(conditionName), StringUtils.substring(conditionName, 0, 15));
    }

    private static String createIssueSatisfyingAllConditions(TestUser user)
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

    private static void watchIssue(TestUser user, String issueKey)
    {
        WatchersClient watchersClient = new WatchersClient(product.environmentData());
        watchersClient.postResponse(issueKey, user.getUsername());
    }

    private static void voteIssue(TestUser user, String issueKey)
    {
        VotesClient votesClient = new VotesClient(product.environmentData());
        votesClient.loginAs(user.getUsername(), user.getPassword());
        votesClient.postResponse(issueKey);
    }

    private boolean isItemPresentInMoreActionsMenu(IssueMenu issueMenu, String webItemTitle)
    {
        return issueMenu.isItemPresentInMoreActionsMenu(webItemTitle);
    }
}
