package com.atlassian.plugin.connect.jira.web.condition;

import com.atlassian.application.api.ApplicationKey;
import com.atlassian.jira.application.ApplicationAuthorizationService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class CanUseApplicationTest {

    private static final ApplicationUser USER = mock(ApplicationUser.class);
    private static final ApplicationKey APPLICATION_KEY = ApplicationKey.valueOf("jira-app");

    private final JiraAuthenticationContext jiraAuthenticationContext = mock(JiraAuthenticationContext.class);
    private final ApplicationAuthorizationService applicationAuthorizationService = mock(ApplicationAuthorizationService.class);

    private final CanUseApplication condition = new CanUseApplication(applicationAuthorizationService, jiraAuthenticationContext);

    @Before
    public void setUp() {
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(USER);
    }

    @Test
    public void conditionEvaluatesToTrueIfApplicationIsInstalledAndUserIsLicensed() {
        initConditionWith(APPLICATION_KEY.value());
        when(applicationAuthorizationService.canUseApplication(USER, APPLICATION_KEY)).thenReturn(true);
        when(applicationAuthorizationService.isApplicationInstalledAndLicensed(APPLICATION_KEY)).thenReturn(true);
        assertTrue(condition.shouldDisplay(emptyMap()));
    }

    @Test
    public void conditionEvaluatesToTrueIfApplicationIsInstalledButUserIsNotLicensed() {
        initConditionWith(APPLICATION_KEY.value());
        when(applicationAuthorizationService.isApplicationInstalledAndLicensed(APPLICATION_KEY)).thenReturn(true);
        assertFalse(condition.shouldDisplay(emptyMap()));
    }

    @Test
    public void conditionEvaluatesToTrueIfUserIsLicensedButApplicationIsNotInstalled() {
        initConditionWith(APPLICATION_KEY.value());
        when(applicationAuthorizationService.canUseApplication(USER, APPLICATION_KEY)).thenReturn(true);
        assertFalse(condition.shouldDisplay(emptyMap()));
    }

    @Test
    public void conditionEvaluatesToFalseAutomaticallyIfKeyIsInvalid() {
        initConditionWith("///;;;'[][][$%#$%,,, ---  =>");
        assertFalse(condition.shouldDisplay(emptyMap()));
        verifyNoMoreInteractions(applicationAuthorizationService);
    }


    private void initConditionWith(String applicationKey) {
        condition.init(ImmutableMap.of("applicationKey", applicationKey));
    }

}
