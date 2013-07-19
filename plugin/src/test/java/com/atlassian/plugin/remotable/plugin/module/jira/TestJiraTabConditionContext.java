package com.atlassian.plugin.remotable.plugin.module.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issuetabpanel.ShowPanelRequest;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.conditions.HasIssuePermissionCondition;
import com.atlassian.jira.plugin.webfragment.conditions.HasProjectPermissionCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.project.browse.BrowseProjectContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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
import static org.mockito.Mockito.when;

/**
 * Tests if the context for conditions contains expected variables.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Permissions.class, ApplicationUsers.class})
public class TestJiraTabConditionContext
{
    @Mock
    PermissionManager permissionManager;

    @Mock
    Issue issue;

    @Mock
    ApplicationUser applicationUser;

    @Mock
    User user;

    @Mock
    Project project;

    @Mock
    UserUtil userUtil;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(ApplicationUsers.class);
        PowerMockito.mockStatic(Permissions.class);
    }

    @Test
    public void testDisplayIssueTabIfUserHasCreateIssuePermission()
    {
        PowerMockito.when(ApplicationUsers.from(user)).thenReturn(applicationUser);
        PowerMockito.when(Permissions.getType(Mockito.anyString())).thenReturn(CREATE_ISSUE);
        when(permissionManager.hasPermission(CREATE_ISSUE, issue, applicationUser)).thenReturn(true);

        ShowPanelRequest showPanelRequest = new ShowPanelRequest(issue, user);
        final Map<String,Object> conditionContext = JiraTabConditionContext.createConditionContext(showPanelRequest);

        assertTrue("Condition context should contain JIRA helper", conditionContext.containsKey(CONTEXT_KEY_HELPER));
        assertTrue("Condition context should contain the user", conditionContext.containsKey(JiraWebInterfaceManager.CONTEXT_KEY_USER));
        assertThat(conditionContext.get(CONTEXT_KEY_HELPER), instanceOf(JiraHelper.class));

        Condition hasIssuePermissionCondition = new HasIssuePermissionCondition(permissionManager);
        hasIssuePermissionCondition.init(ImmutableMap.<String, String>of());
        assertTrue("User should see the issue tab", hasIssuePermissionCondition.shouldDisplay(conditionContext));
    }

    @Test
    public void testDoNotDisplayIssueTabIfUserNotLoggedIn()
    {
        PowerMockito.when(Permissions.getType(Mockito.anyString())).thenReturn(PROJECT_ADMIN);
        ComponentAccessor.initialiseWorker(Mockito.mock(ComponentAccessor.Worker.class));
        PowerMockito.when(ComponentAccessor.getUserUtil()).thenReturn(userUtil);
        when(userUtil.getUserByName(null)).thenReturn(null);
        when(permissionManager.hasPermission(CREATE_ISSUE, project, applicationUser)).thenReturn(true);

        ShowPanelRequest showPanelRequest = new ShowPanelRequest(issue, null);
        final Map<String,Object> conditionContext = JiraTabConditionContext.createConditionContext(showPanelRequest);

        assertTrue("Condition context should contain JIRA helper", conditionContext.containsKey(CONTEXT_KEY_HELPER));
        assertFalse("Condition context shouldn't contain the user", conditionContext.containsKey(JiraWebInterfaceManager.CONTEXT_KEY_USER));
        assertThat(conditionContext.get(CONTEXT_KEY_HELPER), instanceOf(JiraHelper.class));

        JiraHelper helper = (JiraHelper) conditionContext.get(CONTEXT_KEY_HELPER);
        assertNotNull("Issue in JIRA helper params should not be null", helper.getContextParams().get("issue"));

        Condition hasIssuePermissionCondition = new HasIssuePermissionCondition(permissionManager);
        hasIssuePermissionCondition.init(ImmutableMap.<String, String>of());
        assertFalse("User should see the project tab", hasIssuePermissionCondition.shouldDisplay(conditionContext));
    }

    @Test
    public void testDoNotDisplayIssueTabIfUserDoesNotHavePermissions()
    {
        PowerMockito.when(Permissions.getType(Mockito.anyString())).thenReturn(PROJECT_ADMIN);
        PowerMockito.when(ApplicationUsers.from(user)).thenReturn(applicationUser);
        when(permissionManager.hasPermission(CREATE_ISSUE, project, applicationUser)).thenReturn(false);

        ShowPanelRequest showPanelRequest = new ShowPanelRequest(issue, user);
        final Map<String,Object> conditionContext = JiraTabConditionContext.createConditionContext(showPanelRequest);

        assertTrue("Condition context should contain JIRA helper", conditionContext.containsKey(CONTEXT_KEY_HELPER));
        assertTrue("Condition context shouldn contain the user", conditionContext.containsKey(JiraWebInterfaceManager.CONTEXT_KEY_USER));
        assertThat(conditionContext.get(CONTEXT_KEY_HELPER), instanceOf(JiraHelper.class));

        JiraHelper helper = (JiraHelper) conditionContext.get(CONTEXT_KEY_HELPER);
        assertNotNull("Issue in JIRA helper params should not be null", helper.getContextParams().get("issue"));

        Condition hasIssuePermissionCondition = new HasIssuePermissionCondition(permissionManager);
        hasIssuePermissionCondition.init(ImmutableMap.<String, String>of());
        assertFalse("User should see the project tab", hasIssuePermissionCondition.shouldDisplay(conditionContext));
    }

    @Test
    public void testDisplayProjectTabIfUserHasProjectAdminPermission()
    {
        PowerMockito.when(ApplicationUsers.from(user)).thenReturn(applicationUser);
        PowerMockito.when(Permissions.getType(Mockito.anyString())).thenReturn(PROJECT_ADMIN);
        when(permissionManager.hasPermission(PROJECT_ADMIN, project, applicationUser)).thenReturn(true);

        BrowseContext browseContext = new BrowseProjectContext(user, project);
        final Map<String,Object> conditionContext = JiraTabConditionContext.createConditionContext(browseContext);

        assertTrue("Condition context should contain JIRA helper", conditionContext.containsKey(CONTEXT_KEY_HELPER));
        assertTrue("Condition context should contain the user", conditionContext.containsKey(JiraWebInterfaceManager.CONTEXT_KEY_USER));
        assertThat(conditionContext.get(CONTEXT_KEY_HELPER), instanceOf(JiraHelper.class));

        JiraHelper helper = (JiraHelper) conditionContext.get(CONTEXT_KEY_HELPER);
        assertEquals(project, helper.getProjectObject());

        Condition hasProjectPermissionCondition = new HasProjectPermissionCondition(permissionManager);
        hasProjectPermissionCondition.init(ImmutableMap.<String, String>of());
        assertTrue("User should see the project tab", hasProjectPermissionCondition.shouldDisplay(conditionContext));
    }

    @Test
    public void testDoNotDisplayProjectTabIfUserNotLoggedIn()
    {
        PowerMockito.when(Permissions.getType(Mockito.anyString())).thenReturn(PROJECT_ADMIN);
        ComponentAccessor.initialiseWorker(Mockito.mock(ComponentAccessor.Worker.class));
        PowerMockito.when(ComponentAccessor.getUserUtil()).thenReturn(userUtil);
        when(userUtil.getUserByName(null)).thenReturn(null);
        when(permissionManager.hasPermission(PROJECT_ADMIN, project, applicationUser)).thenReturn(true);

        BrowseContext browseContext = new BrowseProjectContext(null, project);
        final Map<String,Object> conditionContext = JiraTabConditionContext.createConditionContext(browseContext);

        assertTrue("Condition context should contain JIRA helper", conditionContext.containsKey(CONTEXT_KEY_HELPER));
        assertFalse("Condition context shouldn't contain the user", conditionContext.containsKey(JiraWebInterfaceManager.CONTEXT_KEY_USER));
        assertThat(conditionContext.get(CONTEXT_KEY_HELPER), instanceOf(JiraHelper.class));

        JiraHelper helper = (JiraHelper) conditionContext.get(CONTEXT_KEY_HELPER);
        assertEquals(project, helper.getProjectObject());

        Condition hasProjectPermissionCondition = new HasProjectPermissionCondition(permissionManager);
        hasProjectPermissionCondition.init(ImmutableMap.<String, String>of());
        assertFalse("User should see the project tab", hasProjectPermissionCondition.shouldDisplay(conditionContext));
    }

    @Test
    public void testDoNotDisplayProjectTabIfUserDoesNotHavePermissions()
    {
        PowerMockito.when(Permissions.getType(Mockito.anyString())).thenReturn(PROJECT_ADMIN);
        PowerMockito.when(ApplicationUsers.from(user)).thenReturn(applicationUser);
        when(permissionManager.hasPermission(PROJECT_ADMIN, project, applicationUser)).thenReturn(false);

        BrowseContext showPanelRequest = new BrowseProjectContext(user, project);
        final Map<String,Object> conditionContext = JiraTabConditionContext.createConditionContext(showPanelRequest);

        assertTrue("Condition context should contain JIRA helper", conditionContext.containsKey(CONTEXT_KEY_HELPER));
        assertTrue("Condition context should contain the user", conditionContext.containsKey(JiraWebInterfaceManager.CONTEXT_KEY_USER));
        assertThat(conditionContext.get(CONTEXT_KEY_HELPER), instanceOf(JiraHelper.class));

        JiraHelper helper = (JiraHelper) conditionContext.get(CONTEXT_KEY_HELPER);
        assertNotNull("Project in JIRA helper can't be null", helper.getProjectObject());

        Condition hasIssuePermissionCondition = new HasProjectPermissionCondition(permissionManager);
        hasIssuePermissionCondition.init(ImmutableMap.<String, String>of());
        assertFalse("User should see the project tab", hasIssuePermissionCondition.shouldDisplay(conditionContext));
    }

}
