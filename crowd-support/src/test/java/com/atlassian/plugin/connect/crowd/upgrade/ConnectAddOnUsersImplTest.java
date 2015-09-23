package com.atlassian.plugin.connect.crowd.upgrade;

import java.util.List;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.crowd.usermanagement.ConnectAddOnUsersImpl;
import com.atlassian.plugin.connect.crowd.usermanagement.CrowdApplicationProvider;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.collection.IsIterableWithSize;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static com.atlassian.crowd.search.query.entity.EntityQuery.ALL_RESULTS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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
    private ConnectAddOnUsersImpl connectAddOnUsers;

    @Mock
    private Application application;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private CrowdApplicationProvider crowdApplicationProvider;
    @Mock
    private ConnectAddonRegistry connectAddonRegistry;

    @Before
    public void beforeEach() throws ApplicationNotFoundException
    {
        initMocks(this);

        List<User> allAddonUsers = asList(mockUser("addon_rad-jira-addon"), mockUser("addon_rad-confluence-addon"));
        when(applicationService.searchDirectGroupRelationships(any(Application.class), any(MembershipQuery.class))).thenReturn(allAddonUsers);
        when(connectAddonRegistry.getAllAddonKeys()).thenReturn(singletonList("rad-jira-addon"));
        when(crowdApplicationProvider.getCrowdApplication()).thenReturn(application);

        connectAddOnUsers = new ConnectAddOnUsersImpl(connectAddonRegistry, applicationService, crowdApplicationProvider);
    }

    @Test
    public void getAddonUsersForHostProductFiltersFromMembersOfAddonsGroup()
            throws ApplicationNotFoundException
    {
        connectAddOnUsers.getAddonUsers();

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
    public void getAddonUsersForHostProductRetrievesAddonsForRegisteredAddonUsers()
            throws ApplicationNotFoundException
    {
        Iterable<User> users = connectAddOnUsers.getAddonUsers();

        assertThat(users, IsIterableWithSize.<User>iterableWithSize(1));
        assertThat(users, contains(hasName("addon_rad-jira-addon")));
    }

    private Matcher<User> hasName(final String name)
    {
        return new TypeSafeMatcher<User>()
        {
            @Override
            protected boolean matchesSafely(User user)
            {
                return user != null && user.getName().equals(name);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Expected user with name ").appendValue(name);
            }
        };
    }

    private static User mockUser(String name)
    {
        User mockUser = mock(User.class);
        when(mockUser.getName()).thenReturn(name);
        return mockUser;
    }
}
