package com.atlassian.plugin.connect.plugin.upgrade;

import java.util.List;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonManager;
import com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserGroupProvisioningService;

import org.hamcrest.collection.IsIterableWithSize;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static com.atlassian.crowd.search.query.entity.EntityQuery.ALL_RESULTS;
import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ConnectAddOnUsersImplTest
{
    private ConnectAddOnUsers connectAddOnUsers;

    @Mock
    private Application application;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private ConnectAddonManager connectAddonManager;
    @Mock
    private ConnectAddOnUserGroupProvisioningService userGroupProvisioningService;

    @Before
    public void beforeEach() throws ApplicationNotFoundException
    {
        initMocks(this);

        List<User> allAddonUsers = asList(mockUser("addon_rad-jira-addon"), mockUser("addon_rad-confluence-addon"));
        when(applicationService.searchDirectGroupRelationships(any(Application.class), any(MembershipQuery.class))).thenReturn(allAddonUsers);
        when(connectAddonManager.getAllAddonKeys()).thenReturn(singletonList("rad-jira-addon"));
        when(userGroupProvisioningService.getCrowdApplication()).thenReturn(application);

        connectAddOnUsers = new ConnectAddOnUsersImpl(connectAddonManager, applicationService, userGroupProvisioningService);
    }

    @Test
    public void filtersFromMembersOfAddonsGroup()
            throws ApplicationNotFoundException
    {
        connectAddOnUsers.getAddonUsersToUpgradeForHostProduct();

        ArgumentCaptor<MembershipQuery> userQueryCaptor = ArgumentCaptor.forClass(MembershipQuery.class);
        verify(applicationService).searchDirectGroupRelationships(eq(application), userQueryCaptor.capture());

        @SuppressWarnings ("unchecked")
        MembershipQuery<User> userQuery = userQueryCaptor.getValue();

        assertThat(userQuery, notNullValue());
        assertThat(userQuery.isFindChildren(), is(true));
        assertThat(userQuery.getEntityNameToMatch(), is("atlassian-addons"));
        assertThat(userQuery.getEntityToMatch(), is(EntityDescriptor.group()));
        assertThat(userQuery.getMaxResults(), is(ALL_RESULTS));
    }

    @Test
    public void retrievesAddonsForRegisteredAddonUsers()
            throws ApplicationNotFoundException
    {
        Iterable<User> users = connectAddOnUsers.getAddonUsersToUpgradeForHostProduct();

        assertThat(users, IsIterableWithSize.<User>iterableWithSize(1));
        assertThat(getOnlyElement(users).getName(), is("addon_rad-jira-addon"));
    }

    private static User mockUser(String name)
    {
        User mockUser = mock(User.class);
        when(mockUser.getName()).thenReturn(name);
        return mockUser;
    }
}
