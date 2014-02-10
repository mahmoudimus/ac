package com.atlassian.plugin.connect.test.plugin;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnUserGroupsService;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnUserService;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddOnUserServiceImpl;
import com.google.common.collect.ImmutableSet;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConnectAddOnUserServiceImplTest
{
    private static final String GROUP_KEY = "atlassian-addons";
    private static final String ADD_ON_EMAIL_ADDRESS = "noreply@mailer.atlassian.com";
    private ConnectAddOnUserService connectAddOnUserService;

    private @Mock ApplicationService applicationService;
    private @Mock ApplicationManager applicationManager;
    private @Mock Application application;
    private @Mock User user;
    private @Mock ConnectAddOnUserGroupsService connectAddOnUserGroupsService;

    @Captor
    private ArgumentCaptor<String> captor;

    private static final String ADD_ON_KEY = "my-cool-thingamajig";
    private static final String USER_KEY = "addon_my-cool-thingamajig";

    @Test
    public void returnsCorrectUserKeyWhenItCreatesTheUser() throws ConnectAddOnUserInitException
    {
        assertThat(connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY), is(USER_KEY));
    }

    @Test
    public void findsUserByKey() throws ConnectAddOnUserInitException, UserNotFoundException
    {
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY);
        verify(applicationService).findUserByName(eq(application), eq(USER_KEY));
    }

    @Test
    public void returnsCorrectUserKeyWhenTheUserAlreadyExists() throws ConnectAddOnUserInitException, UserNotFoundException, InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException
    {
        theUserExists();
        assertThat(connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY), is(USER_KEY));
    }

    @Test
    public void userIsAddedToGroupWhenItCreatesTheUser() throws ConnectAddOnUserInitException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException, MembershipAlreadyExistsException
    {
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY);
        verify(applicationService).addUserToGroup(eq(application), eq(USER_KEY), eq(GROUP_KEY));
    }

    @Test
    public void userIsAddedToGroupWhenTheUserAlreadyExistsButIsNotAMember() throws ConnectAddOnUserInitException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException, MembershipAlreadyExistsException, InvalidCredentialException, InvalidUserException
    {
        theUserExists();
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY);
        verify(applicationService).addUserToGroup(eq(application), eq(USER_KEY), eq(GROUP_KEY));
    }

    @Test
    public void userIsNotAddedToGroupWhenItAlreadyExistsInThatGroup() throws UserNotFoundException, InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException, ConnectAddOnUserInitException, GroupNotFoundException, MembershipAlreadyExistsException
    {
        theUserExists();
        when(applicationService.isUserDirectGroupMember(eq(application), eq(USER_KEY), eq(GROUP_KEY))).thenReturn(true);
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY);
        verify(applicationService, never()).addUserToGroup(eq(application), eq(USER_KEY), eq(GROUP_KEY));
    }

    @Test
    public void userIsCreatedWithCorrectEmailAddress() throws ConnectAddOnUserInitException, InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException
    {
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY);
        verify(applicationService).addUser(eq(application), argThat(hasExpectedEmailAddress()), any(PasswordCredential.class));
    }

    @Test
    public void userEmailIsCorrectedIfFoundChanged() throws ConnectAddOnUserInitException, UserNotFoundException, InvalidUserException, InvalidCredentialException, ApplicationPermissionException, OperationFailedException
    {
        theUserExists();
        when(user.getEmailAddress()).thenReturn("wrong");
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY);
        verify(applicationService).updateUser(eq(application), argThat(hasExpectedEmailAddress()));
    }

    @Test
    public void userIsCreatedWithDefaultProductGroups() throws ConnectAddOnUserInitException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException, MembershipAlreadyExistsException
    {
        when(connectAddOnUserGroupsService.getDefaultProductGroups()).thenReturn(ImmutableSet.of("product group"));
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY);
        verify(applicationService, times(2)).addUserToGroup(eq(application), eq(USER_KEY), captor.capture());
        assertThat(captor.getAllValues(), containsInAnyOrder(GROUP_KEY, "product group"));
    }

    @Test
    public void userIsAddedToDefaultProductGroupsIfItExistedAndWasNotAMember() throws UserNotFoundException, InvalidUserException, InvalidCredentialException, ApplicationPermissionException, OperationFailedException, ConnectAddOnUserInitException, GroupNotFoundException, MembershipAlreadyExistsException
    {
        theUserExists();
        when(connectAddOnUserGroupsService.getDefaultProductGroups()).thenReturn(ImmutableSet.of("product group"));
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY);
        verify(applicationService, times(2)).addUserToGroup(eq(application), eq(USER_KEY), captor.capture());
        assertThat(captor.getAllValues(), containsInAnyOrder(GROUP_KEY, "product group"));
    }

    @Test
    public void userIsNotAddedToDefaultProductGroupsIfItWasAlreadyAMember() throws UserNotFoundException, InvalidUserException, InvalidCredentialException, ApplicationPermissionException, OperationFailedException, ConnectAddOnUserInitException, GroupNotFoundException, MembershipAlreadyExistsException
    {
        theUserExists();
        when(connectAddOnUserGroupsService.getDefaultProductGroups()).thenReturn(ImmutableSet.of("product group"));
        when(applicationService.isUserDirectGroupMember(eq(application), eq(USER_KEY), eq("product group"))).thenReturn(true);
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY);
        verify(applicationService, never()).addUserToGroup(eq(application), eq(USER_KEY), eq("product group"));
    }

    private ArgumentMatcher<UserTemplate> hasExpectedEmailAddress()
    {
        return new ArgumentMatcher<UserTemplate>()
        {
            @Override
            public boolean matches(Object argument)
            {
                return argument instanceof UserTemplate && ADD_ON_EMAIL_ADDRESS.equals(((UserTemplate) argument).getEmailAddress());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText(UserTemplate.class.getSimpleName() + " with the email address").appendValue(ADD_ON_EMAIL_ADDRESS);
            }
        };
    }

    private void theUserExists() throws UserNotFoundException, InvalidUserException, OperationFailedException, InvalidCredentialException, ApplicationPermissionException
    {
        when(applicationService.findUserByName(eq(application), eq(USER_KEY))).thenReturn(user);
        when(applicationService.addUser(any(Application.class), any(UserTemplate.class), any(PasswordCredential.class))).thenThrow(new IllegalArgumentException("the code should not create a user"));
    }

    @Before
    public void beforeEachTest() throws ApplicationNotFoundException, InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException
    {
        when(applicationManager.findByName("crowd-embedded")).thenReturn(application);
        when(applicationService.addUser(eq(application), eq(new UserTemplate(USER_KEY)), eq(PasswordCredential.NONE))).thenReturn(user);
        when(user.getName()).thenReturn(USER_KEY);
        when(connectAddOnUserGroupsService.getDefaultProductGroups()).thenReturn(Collections.<String>emptySet());
        connectAddOnUserService = new ConnectAddOnUserServiceImpl(applicationService, applicationManager, connectAddOnUserGroupsService);
    }
}
