package com.atlassian.plugin.connect.jira.web.condition;

import com.atlassian.application.api.ApplicationKey;
import com.atlassian.jira.application.ApplicationAuthorizationService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginParseException;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CanUseApplicationConditionTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final ApplicationUser USER = mock(ApplicationUser.class);
    private static final ApplicationKey APPLICATION_KEY = ApplicationKey.valueOf("jira-app");

    private final JiraAuthenticationContext jiraAuthenticationContext = mock(JiraAuthenticationContext.class);
    private final ApplicationAuthorizationService applicationAuthorizationService = mock(ApplicationAuthorizationService.class);

    private final CanUseApplicationCondition condition = new CanUseApplicationCondition(applicationAuthorizationService, jiraAuthenticationContext);

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
    public void jiraCoreIsAlwaysTreatedAsInstalled() {
        ApplicationKey key = ApplicationKey.valueOf("jira-core");
        initConditionWith(key.value());
        when(applicationAuthorizationService.canUseApplication(USER, key)).thenReturn(true);
        when(applicationAuthorizationService.isApplicationInstalledAndLicensed(key)).thenReturn(false);
        assertTrue(condition.shouldDisplay(emptyMap()));
    }

    @Test
    public void conditionEvaluatesToFalseIfApplicationIsInstalledButUserIsNotLicensed() {
        initConditionWith(APPLICATION_KEY.value());
        when(applicationAuthorizationService.isApplicationInstalledAndLicensed(APPLICATION_KEY)).thenReturn(true);

        assertFalse(condition.shouldDisplay(emptyMap()));
    }

    @Test
    public void conditionEvaluatesToFalseIfUserIsLicensedButApplicationIsNotInstalled() {
        initConditionWith(APPLICATION_KEY.value());
        when(applicationAuthorizationService.canUseApplication(USER, APPLICATION_KEY)).thenReturn(true);

        assertFalse(condition.shouldDisplay(emptyMap()));
    }

    @Test
    public void ExceptionIsThrownIfKeyIsInvalid() {
        thrown.expect(PluginParseException.class);
        thrown.expectMessage("invalid application key: \"///;;;'[][][$%#$%,,, ---  =>\"");
        initConditionWith("///;;;'[][][$%#$%,,, ---  =>");
    }

    @Test
    public void ExceptionIsThrownIfKeyIsNull() {
        thrown.expect(PluginParseException.class);
        thrown.expectMessage("\"applicationKey\" parameter is required in the can_use_application condition");
        initConditionWith(null);
    }

    private void initConditionWith(String applicationKey) {
        Map<String, String> properties = Maps.newHashMap();
        properties.put("applicationKey", applicationKey);
        condition.init(properties);
    }
}
