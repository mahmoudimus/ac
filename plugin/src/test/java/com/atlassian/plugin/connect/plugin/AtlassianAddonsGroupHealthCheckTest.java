package com.atlassian.plugin.connect.plugin;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.user.User;
import com.atlassian.healthcheck.core.HealthStatus;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.Constants;
import com.google.common.collect.Sets;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class AtlassianAddonsGroupHealthCheckTest
{
    @Mock
    private ApplicationManager applicationManager;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private ConnectAddOnUserGroupProvisioningService groupProvisioningService;

    @Test
    public void testHealthyIfNoAddonUsers() throws Exception
    {
        AtlassianAddonsGroupHealthCheck healthCheck = createHealthCheckWithUsers(Sets.<User>newHashSet());
        assertTrue(healthCheck.check().isHealthy());
    }

    @Test
    public void testHealthyIfAddonUsersHaveCorrectUsernameAndEmail() throws Exception
    {
        HashSet<User> users = Sets.newHashSet();
        users.add(createUser("addon_my-addon", Constants.ADDON_USER_EMAIL_ADDRESS));
        users.add(createUser("addon_my-another-addon", Constants.ADDON_USER_EMAIL_ADDRESS));

        AtlassianAddonsGroupHealthCheck healthCheck = createHealthCheckWithUsers(users);
        HealthStatus check = healthCheck.check();
        assertTrue(check.isHealthy());
    }

    @Test
    public void testUnhealthyIfAddonUserHasIncorrectEmail() throws Exception
    {
        HashSet<User> users = Sets.newHashSet();
        users.add(createUser("addon_my-addon", "ceo@acme.com"));

        AtlassianAddonsGroupHealthCheck healthCheck = createHealthCheckWithUsers(users);
        HealthStatus check = healthCheck.check();
        assertFalse(check.isHealthy());
        assertThat(check.failureReason(), containsString("unexpected email"));
    }

    @Test
    public void testUnhealthyIfAddonUserHasIncorrectUsername() throws Exception
    {
        HashSet<User> users = Sets.newHashSet();
        users.add(createUser("larryemdur", Constants.ADDON_USER_EMAIL_ADDRESS));

        AtlassianAddonsGroupHealthCheck healthCheck = createHealthCheckWithUsers(users);
        HealthStatus check = healthCheck.check();
        assertFalse(check.isHealthy());
        assertThat(check.failureReason(), containsString("unexpected username"));
    }

    @Test
    public void testUnhealthyIfSomeAddonUsersHaveIncorrectEmail() throws Exception
    {
        HashSet<User> users = Sets.newHashSet();
        users.add(createUser("addon_my-addon", Constants.ADDON_USER_EMAIL_ADDRESS));
        users.add(createUser("addon_my-baddie", "ceo@acme.com"));

        AtlassianAddonsGroupHealthCheck healthCheck = createHealthCheckWithUsers(users);
        HealthStatus check = healthCheck.check();
        assertFalse(check.isHealthy());
        assertThat(check.failureReason(), containsString("unexpected email"));
    }

    @Test
    public void testUnhealthyIfSomeAddonUsersHaveIncorrectUsername() throws Exception
    {
        HashSet<User> users = Sets.newHashSet();
        users.add(createUser("addon_my-addon", Constants.ADDON_USER_EMAIL_ADDRESS));
        users.add(createUser("larryemdur", Constants.ADDON_USER_EMAIL_ADDRESS));

        AtlassianAddonsGroupHealthCheck healthCheck = createHealthCheckWithUsers(users);
        HealthStatus check = healthCheck.check();
        assertFalse(check.isHealthy());
        assertThat(check.failureReason(), containsString("unexpected username"));
    }

    @Test
    public void testUnhealthyIfSomeAddonUsersHaveIncorrectUsernameAndEmail() throws Exception
    {
        HashSet<User> users = Sets.newHashSet();
        users.add(createUser("addon_my-addon", Constants.ADDON_USER_EMAIL_ADDRESS));
        users.add(createUser("larryemdur", "larry@thepriceisright.com"));

        AtlassianAddonsGroupHealthCheck healthCheck = createHealthCheckWithUsers(users);
        HealthStatus check = healthCheck.check();
        assertFalse(check.isHealthy());
        assertThat(check.failureReason(), allOf(containsString("unexpected username"), containsString("unexpected email")));
    }

    @Test
    public void testUnhealthyIfSomeAddonUsersHaveIncorrectUsernameOrEmail() throws Exception
    {
        HashSet<User> users = Sets.newHashSet();
        users.add(createUser("addon_my-addon", Constants.ADDON_USER_EMAIL_ADDRESS));
        users.add(createUser("addon_price-is-right", "larry@thepriceisright.com"));
        users.add(createUser("larry", Constants.ADDON_USER_EMAIL_ADDRESS));

        AtlassianAddonsGroupHealthCheck healthCheck = createHealthCheckWithUsers(users);
        HealthStatus check = healthCheck.check();
        assertFalse(check.isHealthy());
        assertThat(check.failureReason(), allOf(containsString("unexpected username"), containsString("unexpected email")));
    }

    @Test
    public void testUnhealthyIfSomeAddonUsersHaveIncorrectUsernameOrEmailOrActive() throws Exception
    {
        HashSet<User> users = Sets.newHashSet();
        users.add(createUser("addon_my-addon", Constants.ADDON_USER_EMAIL_ADDRESS));
        users.add(createUser("addon_price-is-right", "larry@thepriceisright.com"));
        users.add(createUser("larry", Constants.ADDON_USER_EMAIL_ADDRESS));
        users.add(createUser("addon_family-fued", Constants.ADDON_USER_EMAIL_ADDRESS));

        AtlassianAddonsGroupHealthCheck healthCheck = createHealthCheckWithUsers(users);
        HealthStatus check = healthCheck.check();
        assertFalse(check.isHealthy());
        // TODO: assert that we can find users that were never associated with an add-on (e.g. by setting attributes on users)
        assertThat(check.failureReason(), allOf(containsString("unexpected username"), containsString("unexpected email")));
    }

    @Ignore("TODO: assert that we can find users that were never associated with an add-on (e.g. by setting attributes on users)")
    @Test
    public void testUnhealthyIfAddonUserNotInApplinkAndActive() throws Exception
    {
        HashSet<User> users = Sets.newHashSet();
        users.add(createUser("addon_my-addon", Constants.ADDON_USER_EMAIL_ADDRESS, true));

        AtlassianAddonsGroupHealthCheck healthCheck = createHealthCheckWithUsers(users);
        HealthStatus check = healthCheck.check();
        assertFalse(check.isHealthy());
        assertThat(check.failureReason(), containsString("no applink"));
    }

    private User createUser(String username, String email)
    {
        return createUser(username, email, true);
    }

    private User createUser(String username, String email, boolean isActive)
    {
        User u = mock(User.class);
        when(u.getName()).thenReturn(username);
        when(u.getEmailAddress()).thenReturn(email);
        when(u.isActive()).thenReturn(isActive);
        return u;
    }

    private AtlassianAddonsGroupHealthCheck createHealthCheckWithUsers(Collection<User> users)
    {
        return new TestHealthCheck(users, applicationManager, applicationService, groupProvisioningService);
    }

    private static class TestHealthCheck extends AtlassianAddonsGroupHealthCheck
    {
        private final Collection<User> users;

        TestHealthCheck(final Collection<User> users, ApplicationManager applicationManager, ApplicationService applicationService,
                ConnectAddOnUserGroupProvisioningService groupProvisioningService)
        {
            super(applicationManager, applicationService, groupProvisioningService);
            this.users = users;
        }

        @Override
        protected Collection<User> getAddonUsers() throws ApplicationNotFoundException
        {
            return users;
        }
    }
}
