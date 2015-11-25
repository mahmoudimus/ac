package com.atlassian.plugin.connect.jira.web.condition;

import java.util.Map;

import com.atlassian.application.api.ApplicationKey;
import com.atlassian.jira.application.ApplicationAuthorizationService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class CanUseApplicationConditionTest
{
    private static final ApplicationUser USER = mock(ApplicationUser.class);
    private static final ApplicationKey APPLICATION_KEY = ApplicationKey.valueOf("jira-app");

    private final JiraAuthenticationContext jiraAuthenticationContext = mock(JiraAuthenticationContext.class);
    private final ApplicationAuthorizationService applicationAuthorizationService = mock(ApplicationAuthorizationService.class);

    private final CanUseApplicationCondition condition = new CanUseApplicationCondition(applicationAuthorizationService, jiraAuthenticationContext);

    @Before
    public void setUp()
    {
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(USER);
    }

    @Test
    public void conditionEvaluatesToTrueIfApplicationIsInstalledAndUserIsLicensed()
    {
        initConditionWith(APPLICATION_KEY.value());
        when(applicationAuthorizationService.canUseApplication(USER, APPLICATION_KEY)).thenReturn(true);
        when(applicationAuthorizationService.isApplicationInstalledAndLicensed(APPLICATION_KEY)).thenReturn(true);

        assertTrue(condition.shouldDisplay(emptyMap()));
    }

    @Test
    public void conditionEvaluatesToFalseIfApplicationIsInstalledButUserIsNotLicensed()
    {
        initConditionWith(APPLICATION_KEY.value());
        when(applicationAuthorizationService.isApplicationInstalledAndLicensed(APPLICATION_KEY)).thenReturn(true);

        assertFalse(condition.shouldDisplay(emptyMap()));
    }

    @Test
    public void conditionEvaluatesToFalseIfUserIsLicensedButApplicationIsNotInstalled()
    {
        initConditionWith(APPLICATION_KEY.value());
        when(applicationAuthorizationService.canUseApplication(USER, APPLICATION_KEY)).thenReturn(true);

        assertFalse(condition.shouldDisplay(emptyMap()));
    }

    @Test
    public void conditionEvaluatesToFalseAutomaticallyIfKeyIsInvalid()
    {
        initConditionWith("///;;;'[][][$%#$%,,, ---  =>");

        assertFalse(condition.shouldDisplay(emptyMap()));
        verifyNoMoreInteractions(applicationAuthorizationService);
    }

    @Test
    public void conditionEvaluatesToFalseAutomaticallyIfKeyIsNull()
    {
        initConditionWith(null);

        assertFalse(condition.shouldDisplay(emptyMap()));
        verifyNoMoreInteractions(applicationAuthorizationService);
    }

    private void initConditionWith(String applicationKey)
    {
        Map<String, String> properties = Maps.newHashMap();
        properties.put("applicationKey", applicationKey);
        condition.init(properties);
    }
}
