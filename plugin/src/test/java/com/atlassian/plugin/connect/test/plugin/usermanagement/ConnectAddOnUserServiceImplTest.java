package com.atlassian.plugin.connect.test.plugin.usermanagement;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.crowd.service.factory.CrowdClientFactory;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.usermanagement.*;
import com.atlassian.plugin.connect.plugin.util.FeatureManager;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.buildAttributeConnectAddOnAttributeName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class ConnectAddOnUserServiceImplTest
{
    private static final String GROUP_KEY = "atlassian-addons";
    private static final String ADD_ON_EMAIL_ADDRESS = "noreply@mailer.atlassian.com";
    private static final String ADD_ON_DISPLAY_NAME = "A Cool Test Add-on";
    private static final String APPLICATION_NAME = "crowd-embedded";

    private @Mock ApplicationService applicationService;
    private @Mock ApplicationManager applicationManager;
    private @Mock Application application;
    private @Mock User user;
    private @Mock ConnectAddOnUserProvisioningService connectAddOnUserProvisioningService;
    private @Mock CrowdClientFactory crowdClientFactory;
    private @Mock CrowdClient crowdClient;
    private @Mock FeatureManager featureManager;

    private ConnectAddOnUserService connectAddOnUserService;

    @SuppressWarnings ("UnusedDeclaration")
    @Captor private ArgumentCaptor<String> captor;
    @Captor private ArgumentCaptor<Map<String, Set<String>>> localAttributeCaptor;
    @Captor private ArgumentCaptor<Map<String, Set<String>>> remoteAttributeCaptor;

    private static final String ADD_ON_KEY = "my-cool-thingamajig";
    private static final String USER_KEY = "addon_my-cool-thingamajig";

    @Test
    public void returnsCorrectUserKeyWhenItCreatesTheUser() throws ConnectAddOnUserInitException
    {
        assertThat(connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME), is(USER_KEY));
    }

    @Test
    public void recoversFromRaceConditionResultingInInvalidUserException() throws InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException, UserNotFoundException
    {
        recoversFromRaceConditionResultingInException(new InvalidUserException(mock(User.class), "foo"));
    }

    @Test
    public void recoversFromRaceConditionResultingInOperationFailedException() throws InvalidCredentialException, InvalidUserException, OperationFailedException, ApplicationPermissionException, UserNotFoundException
    {
        recoversFromRaceConditionResultingInException(new OperationFailedException("foo"));
    }

    @Test
    public void recoversFromRaceConditionResultingInDataIntegrityViolationException() throws InvalidCredentialException, InvalidUserException, OperationFailedException, ApplicationPermissionException, UserNotFoundException
    {
        recoversFromRaceConditionResultingInException(new DataIntegrityViolationException("foo"));
    }

    @Test
    public void findsUserByKey() throws ConnectAddOnUserInitException, UserNotFoundException
    {
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService).findUserByName(eq(application), eq(USER_KEY));
    }

    @Test
    public void returnsCorrectUserKeyWhenTheUserAlreadyExists() throws ConnectAddOnUserInitException, UserNotFoundException, InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException
    {
        theUserExists();
        assertThat(connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME), is(USER_KEY));
    }

    @Test
    public void userIsAddedToGroupWhenItCreatesTheUser() throws ConnectAddOnUserInitException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException, MembershipAlreadyExistsException
    {
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService).addUserToGroup(eq(application), eq(USER_KEY), eq(GROUP_KEY));
    }

    @Test
    public void userIsAddedToGroupWhenTheUserAlreadyExistsButIsNotAMember() throws ConnectAddOnUserInitException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException, MembershipAlreadyExistsException, InvalidCredentialException, InvalidUserException
    {
        theUserExists();
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService).addUserToGroup(eq(application), eq(USER_KEY), eq(GROUP_KEY));
    }

    @Test
    public void userIsNotAddedToGroupWhenItAlreadyExistsInThatGroup() throws UserNotFoundException, InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException, ConnectAddOnUserInitException, GroupNotFoundException, MembershipAlreadyExistsException
    {
        theUserExists();
        when(applicationService.isUserDirectGroupMember(eq(application), eq(USER_KEY), eq(GROUP_KEY))).thenReturn(true);
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService, never()).addUserToGroup(eq(application), eq(USER_KEY), eq(GROUP_KEY));
    }

    @Test
    public void userIsCreatedWithCorrectEmailAddress() throws ConnectAddOnUserInitException, InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException
    {
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService).addUser(eq(application), argThat(hasExpectedEmailAddress()), any(PasswordCredential.class));
    }

    @Test
    public void userEmailIsCorrectedIfFoundChanged() throws ConnectAddOnUserInitException, UserNotFoundException, InvalidUserException, InvalidCredentialException, ApplicationPermissionException, OperationFailedException
    {
        theUserExists();
        when(user.getEmailAddress()).thenReturn("wrong");
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService).updateUser(eq(application), argThat(hasExpectedEmailAddress()));
    }

    @Test
    public void userIsCreatedWithDefaultProductGroups() throws ConnectAddOnUserInitException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException, MembershipAlreadyExistsException
    {
        when(connectAddOnUserProvisioningService.getDefiniteDefaultProductGroups()).thenReturn(ImmutableSet.of("product group"));
        when(connectAddOnUserProvisioningService.getPossibleDefaultProductGroups()).thenReturn(Collections.<String>emptySet());
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService, times(2)).addUserToGroup(eq(application), eq(USER_KEY), captor.capture());
        assertThat(captor.getAllValues(), containsInAnyOrder(GROUP_KEY, "product group"));
    }

    @Test
    public void userIsCreatedWithAtlassianConnectUserAttribute()
            throws InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException, UserNotFoundException, InvalidAuthenticationException
    {
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService).addUser(eq(application), argThat(hasExpectedEmailAddress()), any(PasswordCredential.class));
        verify(applicationService).storeUserAttributes(eq(application), eq(USER_KEY), localAttributeCaptor.capture());
        verify(crowdClient).storeUserAttributes(eq(USER_KEY), remoteAttributeCaptor.capture());

        assertTrue(localAttributeCaptor.getValue().get(buildAttributeConnectAddOnAttributeName(APPLICATION_NAME)).contains("true"));
        assertTrue(remoteAttributeCaptor.getValue().get(buildAttributeConnectAddOnAttributeName(APPLICATION_NAME)).contains("true"));
    }

    @Test
    public void userIsCreatedWithAtlassianConnectUserAttributeWhenNotInOnDemand()
            throws InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException, UserNotFoundException, InvalidAuthenticationException
    {
        when(featureManager.isOnDemand()).thenReturn(false);

        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService).addUser(eq(application), argThat(hasExpectedEmailAddress()), any(PasswordCredential.class));
        verify(applicationService).storeUserAttributes(eq(application), eq(USER_KEY), localAttributeCaptor.capture());
        verify(crowdClient, never()).storeUserAttributes(anyString(), anyMap());

        assertTrue(localAttributeCaptor.getValue().get(buildAttributeConnectAddOnAttributeName(APPLICATION_NAME)).contains("true"));
    }

    @Test
    public void userIsAddedToDefaultProductGroupsIfItExistedAndWasNotAMember() throws UserNotFoundException, InvalidUserException, InvalidCredentialException, ApplicationPermissionException, OperationFailedException, ConnectAddOnUserInitException, GroupNotFoundException, MembershipAlreadyExistsException
    {
        theUserExists();
        when(connectAddOnUserProvisioningService.getDefiniteDefaultProductGroups()).thenReturn(ImmutableSet.of("product group"));
        when(connectAddOnUserProvisioningService.getPossibleDefaultProductGroups()).thenReturn(Collections.<String>emptySet());
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService, times(2)).addUserToGroup(eq(application), eq(USER_KEY), captor.capture());
        assertThat(captor.getAllValues(), containsInAnyOrder(GROUP_KEY, "product group"));
    }

    @Test
    public void userIsNotAddedToDefaultProductGroupsIfItWasAlreadyAMember() throws UserNotFoundException, InvalidUserException, InvalidCredentialException, ApplicationPermissionException, OperationFailedException, ConnectAddOnUserInitException, GroupNotFoundException, MembershipAlreadyExistsException
    {
        theUserExists();
        when(connectAddOnUserProvisioningService.getDefiniteDefaultProductGroups()).thenReturn(ImmutableSet.of("product group"));
        when(connectAddOnUserProvisioningService.getPossibleDefaultProductGroups()).thenReturn(Collections.<String>emptySet());
        when(applicationService.isUserDirectGroupMember(eq(application), eq(USER_KEY), eq("product group"))).thenReturn(true);
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService, never()).addUserToGroup(eq(application), eq(USER_KEY), eq("product group"));
    }

    @Test
    public void passwordIsResetIfTheUserExists() throws ConnectAddOnUserInitException, UserNotFoundException, InvalidCredentialException, ApplicationPermissionException, OperationFailedException, InvalidUserException
    {
        theUserExists();
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService).updateUserCredential(eq(application), eq(USER_KEY), eq(PasswordCredential.NONE));
    }

    private void recoversFromRaceConditionResultingInException(Exception exceptionToThrowOnUserCreation) throws InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException, UserNotFoundException
    {
        when(applicationService.addUser(any(Application.class), any(UserTemplate.class), any(PasswordCredential.class))).thenThrow(exceptionToThrowOnUserCreation);
        when(applicationService.findUserByName(eq(application), eq(USER_KEY))).thenReturn(null, mock(User.class));
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService, times(2)).findUserByName(eq(application), eq(USER_KEY));
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
        when(applicationManager.findByName(APPLICATION_NAME)).thenReturn(application);
        when(application.getName()).thenReturn(APPLICATION_NAME);
        when(applicationService.addUser(eq(application), eq(new UserTemplate(USER_KEY)), eq(PasswordCredential.NONE))).thenReturn(user);
        when(user.getName()).thenReturn(USER_KEY);
        when(connectAddOnUserProvisioningService.getDefiniteDefaultProductGroups()).thenReturn(Collections.<String>emptySet());
        when(connectAddOnUserProvisioningService.getPossibleDefaultProductGroups()).thenReturn(Collections.<String>emptySet());
        when(crowdClientFactory.newInstance(any(ClientProperties.class))).thenReturn(crowdClient);
        when(featureManager.isOnDemand()).thenReturn(true);
        ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService = new ConnectAddOnUserGroupProvisioningServiceImpl(applicationService, applicationManager);
        connectAddOnUserService = new ConnectAddOnUserServiceImpl(applicationService, applicationManager, connectAddOnUserProvisioningService, connectAddOnUserGroupProvisioningService, crowdClientFactory, featureManager);
    }
}
