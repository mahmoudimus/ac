package com.atlassian.plugin.remotable.plugin.module.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.plugin.issuetabpanel.ShowPanelRequest;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.conditions.HasIssuePermissionCondition;
import com.atlassian.jira.plugin.webfragment.conditions.HasProjectPermissionCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.project.browse.BrowseProjectContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager.CONTEXT_KEY_HELPER;
import static com.atlassian.jira.security.Permissions.CREATE_ISSUE;
import static com.atlassian.jira.security.Permissions.PROJECT_ADMIN;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

/**
 * Tests if the context for conditions contains expected variables.
 */
@RunWith (MockitoJUnitRunner.class)
public class TestJiraTabConditionContext
{
    @Rule
    public MockComponentContainer container = new MockComponentContainer(this);

    @Mock
    Issue issue;

    @Mock
    ApplicationUser applicationUser;

    @Mock
    User user;

    @Mock
    Project project;

    @Mock
    @AvailableInContainer
    PermissionManager permissionManager;

    @Mock
    @AvailableInContainer
    UserUtil userUtil;

    @Mock
    @AvailableInContainer
    UserKeyService userKeyService;

    @Before
    public void setUp()
    {
        when(userKeyService.getKeyForUsername(anyString())).thenReturn("any_key");
    }

    @Test
    public void testDisplayIssueTabIfUserHasCreateIssuePermission()
    {
        when(permissionManager.hasPermission(eq(CREATE_ISSUE), eq(issue), any(ApplicationUser.class))).thenReturn(true);

        ShowPanelRequest showPanelRequest = new ShowPanelRequest(issue, user);
        final Map<String,Object> conditionContext = JiraTabConditionContext.createConditionContext(showPanelRequest);

        assertTrue("Condition context should contain JIRA helper", conditionContext.containsKey(CONTEXT_KEY_HELPER));
        assertTrue("Condition context should contain the user", conditionContext.containsKey(JiraWebInterfaceManager.CONTEXT_KEY_USER));
        assertThat(conditionContext.get(CONTEXT_KEY_HELPER), instanceOf(JiraHelper.class));

        Condition hasIssuePermissionCondition = new HasIssuePermissionCondition(permissionManager);
        initCondition(hasIssuePermissionCondition, "create");
        assertTrue("User should see the issue tab", hasIssuePermissionCondition.shouldDisplay(conditionContext));
    }

    @Test
    public void testDoNotDisplayIssueTabIfUserNotLoggedIn()
    {
        ShowPanelRequest showPanelRequest = new ShowPanelRequest(issue, null);
        final Map<String,Object> conditionContext = JiraTabConditionContext.createConditionContext(showPanelRequest);

        assertTrue("Condition context should contain JIRA helper", conditionContext.containsKey(CONTEXT_KEY_HELPER));
        assertFalse("Condition context shouldn't contain the user", conditionContext.containsKey(JiraWebInterfaceManager.CONTEXT_KEY_USER));
        assertThat(conditionContext.get(CONTEXT_KEY_HELPER), instanceOf(JiraHelper.class));

        JiraHelper helper = (JiraHelper) conditionContext.get(CONTEXT_KEY_HELPER);
        assertNotNull("Issue in JIRA helper params should not be null", helper.getContextParams().get("issue"));

        Condition hasIssuePermissionCondition = new HasIssuePermissionCondition(permissionManager);
        initCondition(hasIssuePermissionCondition, "create");
        assertFalse("User should not see the issue tab", hasIssuePermissionCondition.shouldDisplay(conditionContext));
    }

    @Test
    public void testDoNotDisplayIssueTabIfUserDoesNotHavePermissions()
    {
        when(permissionManager.hasPermission(eq(CREATE_ISSUE), eq(issue), any(ApplicationUser.class))).thenReturn(false);

        ShowPanelRequest showPanelRequest = new ShowPanelRequest(issue, user);
        final Map<String,Object> conditionContext = JiraTabConditionContext.createConditionContext(showPanelRequest);

        assertTrue("Condition context should contain JIRA helper", conditionContext.containsKey(CONTEXT_KEY_HELPER));
        assertTrue("Condition context shouldn contain the user", conditionContext.containsKey(JiraWebInterfaceManager.CONTEXT_KEY_USER));
        assertThat(conditionContext.get(CONTEXT_KEY_HELPER), instanceOf(JiraHelper.class));

        JiraHelper helper = (JiraHelper) conditionContext.get(CONTEXT_KEY_HELPER);
        assertNotNull("Issue in JIRA helper params should not be null", helper.getContextParams().get("issue"));

        Condition hasIssuePermissionCondition = new HasIssuePermissionCondition(permissionManager);
        initCondition(hasIssuePermissionCondition, "create");
        assertFalse("User should not see the issue tab", hasIssuePermissionCondition.shouldDisplay(conditionContext));
    }

    @Test
    public void testDisplayProjectTabIfUserHasProjectAdminPermission()
    {
        when(permissionManager.hasPermission(eq(PROJECT_ADMIN), eq(project), any(ApplicationUser.class))).thenReturn(true);

        BrowseContext browseContext = new BrowseProjectContext(user, project);
        final Map<String,Object> conditionContext = JiraTabConditionContext.createConditionContext(browseContext);

        assertTrue("Condition context should contain JIRA helper", conditionContext.containsKey(CONTEXT_KEY_HELPER));
        assertTrue("Condition context should contain the user", conditionContext.containsKey(JiraWebInterfaceManager.CONTEXT_KEY_USER));
        assertThat(conditionContext.get(CONTEXT_KEY_HELPER), instanceOf(JiraHelper.class));

        JiraHelper helper = (JiraHelper) conditionContext.get(CONTEXT_KEY_HELPER);
        assertEquals(project, helper.getProjectObject());

        Condition hasProjectPermissionCondition = new HasProjectPermissionCondition(permissionManager);
        initCondition(hasProjectPermissionCondition, "project");
        assertTrue("User should see the project tab", hasProjectPermissionCondition.shouldDisplay(conditionContext));
    }

    @Test
    public void testDoNotDisplayProjectTabIfUserNotLoggedIn()
    {
        BrowseContext browseContext = new BrowseProjectContext(null, project);
        final Map<String,Object> conditionContext = JiraTabConditionContext.createConditionContext(browseContext);

        assertTrue("Condition context should contain JIRA helper", conditionContext.containsKey(CONTEXT_KEY_HELPER));
        assertFalse("Condition context shouldn't contain the user", conditionContext.containsKey(JiraWebInterfaceManager.CONTEXT_KEY_USER));
        assertThat(conditionContext.get(CONTEXT_KEY_HELPER), instanceOf(JiraHelper.class));

        JiraHelper helper = (JiraHelper) conditionContext.get(CONTEXT_KEY_HELPER);
        assertEquals(project, helper.getProjectObject());

        Condition hasProjectPermissionCondition = new HasProjectPermissionCondition(permissionManager);
        initCondition(hasProjectPermissionCondition, "project");
        assertFalse("User should see the project tab", hasProjectPermissionCondition.shouldDisplay(conditionContext));
    }

    @Test
    public void testDoNotDisplayProjectTabIfUserDoesNotHavePermissions()
    {
        when(permissionManager.hasPermission(PROJECT_ADMIN, project, applicationUser)).thenReturn(false);

        BrowseContext showPanelRequest = new BrowseProjectContext(user, project);
        final Map<String,Object> conditionContext = JiraTabConditionContext.createConditionContext(showPanelRequest);

        assertTrue("Condition context should contain JIRA helper", conditionContext.containsKey(CONTEXT_KEY_HELPER));
        assertTrue("Condition context should contain the user", conditionContext.containsKey(JiraWebInterfaceManager.CONTEXT_KEY_USER));
        assertThat(conditionContext.get(CONTEXT_KEY_HELPER), instanceOf(JiraHelper.class));

        JiraHelper helper = (JiraHelper) conditionContext.get(CONTEXT_KEY_HELPER);
        assertNotNull("Project in JIRA helper can't be null", helper.getProjectObject());

        Condition hasIssuePermissionCondition = new HasProjectPermissionCondition(permissionManager);
        initCondition(hasIssuePermissionCondition, "project");
        assertFalse("User should see the project tab", hasIssuePermissionCondition.shouldDisplay(conditionContext));
    }

    private void initCondition(Condition condition, String permissionName)
    {
        condition.init(ImmutableMap.<String, String>of("permission", permissionName));
    }

}
