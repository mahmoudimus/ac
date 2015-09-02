package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.UserTemplate;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestConnectCrowdBase
{
    @Mock private UserReconciliation userReconciliation;

    private MockCrowdImplementation crowdBase;

    @Before
    public void beforeEach()
    {
        initMocks(this);
        when(userReconciliation.getFixes(any(User.class), anyString(), anyString(), anyBoolean())).thenReturn(Optional.<UserTemplate>absent());
    }

    @SuppressWarnings ("unchecked")
    @Test
    public void createOrEnableUserReportsExistingUserAsExisting()
            throws UserNotFoundException
    {
        crowdBase = new MockCrowdImplementation(userReconciliation, new UserTemplate("name"));
        UserCreationResult result = crowdBase.createOrEnableUser("name", "Display Name", "email@address.com", PasswordCredential.NONE);

        assertThat("Existing user is reported existing", result.isNewlyCreated(), is(false));
    }

    @Test
    public void createOrEnableUserReportsNewlyCreatedUserAsNewlyCreated()
            throws UserNotFoundException
    {
        crowdBase = new MockCrowdImplementation(userReconciliation, null);
        UserCreationResult result = crowdBase.createOrEnableUser("name", "Display Name", "email@address.com", PasswordCredential.NONE);

        assertThat("Use was created", crowdBase.user, notNullValue());
        assertThat("User was created with the provided name", crowdBase.user.getName(), is("name"));
        assertThat("User was created with the provided display name", crowdBase.user.getDisplayName(), is("Display Name"));
        assertThat("User was created with the provided email address", crowdBase.user.getEmailAddress(), is("email@address.com"));

        assertThat("Newly created user is reported as newly created", result.isNewlyCreated(), is(true));
    }

    private static class MockCrowdImplementation extends ConnectCrowdBase
    {
        private User user;

        public MockCrowdImplementation(UserReconciliation userReconciliation, User user)
        {
            super(userReconciliation);
            this.user = user;
        }

        @Override
        public void setAttributesOnUser(String username, Map<String, Set<String>> attributes)
        {
        }

        @Override
        public Optional<? extends User> findUserByName(String username)
        {
            if (user != null)
            {
                assertThat("Crowd base searches for the user by name", username, is(user.getName()));
                return of(user);
            }
            else
            {
                return absent();
            }
        }

        @Override
        protected void addUser(UserTemplate userTemplate, PasswordCredential passwordCredential)
                throws OperationFailedException, InvalidUserException
        {
            if (user != null)
            {
                fail("User should not be re-created");
            }
            user = userTemplate;
        }

        @Override
        protected void updateUserCredential(String username, PasswordCredential passwordCredential)
        {
            assertThat("PasswordCredential should be NONE", passwordCredential, is(PasswordCredential.NONE));
        }

        @Override
        protected void updateUser(UserTemplate fixes)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        protected void addGroup(String groupName)
                throws InvalidGroupException, OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void ensureUserIsInGroup(String userKey, String groupKey)
                throws ApplicationNotFoundException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException, InvalidAuthenticationException
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void removeUserFromGroup(String userKey, String groupKey)
                throws ApplicationNotFoundException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException, InvalidAuthenticationException
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Group findGroupByKey(String groupKey)
                throws ApplicationNotFoundException, ApplicationPermissionException, InvalidAuthenticationException
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void invalidateSessions(String username)
        {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}