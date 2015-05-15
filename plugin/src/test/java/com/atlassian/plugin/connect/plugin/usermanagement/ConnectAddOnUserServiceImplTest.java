package com.atlassian.plugin.connect.plugin.usermanagement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.MembershipAlreadyExistsException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
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

import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.buildAttributeConnectAddOnAttributeName;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class ConnectAddOnUserServiceImplTest
{
    private static final String GROUP_KEY = "atlassian-addons";
    private static final String ADD_ON_EMAIL_ADDRESS = "noreply@mailer.atlassian.com";
    private static final String ADD_ON_DISPLAY_NAME = "A Cool Test Add-on";
    private static final String APPLICATION_NAME = "crowd-embedded";
    public static final Set<String> EXPECTED_ATTRIBUTE_VALUE = new HashSet<>(singletonList("true"));

    private @Mock ApplicationService applicationService;
    private @Mock ApplicationManager applicationManager;
    private @Mock Application application;
    private @Mock User user;
    private @Mock ConnectAddOnUserProvisioningService connectAddOnUserProvisioningService;
    private @Mock CrowdClientFacade crowdClientFacade;
    private @Mock CrowdClient crowdClient;
    private @Mock FeatureManager featureManager;

    private ConnectAddOnUserService connectAddOnUserService;

    @SuppressWarnings ("UnusedDeclaration")
    @Captor private ArgumentCaptor<String> captor;

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
    public void userIsCreatedWithDefaultProductGroupsAlwaysExpected() throws ConnectAddOnUserInitException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException, MembershipAlreadyExistsException
    {
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsAlwaysExpected()).thenReturn(ImmutableSet.of("product group"));
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected()).thenReturn(Collections.<String>emptySet());
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService, times(2)).addUserToGroup(eq(application), eq(USER_KEY), captor.capture());
        assertThat(captor.getAllValues(), containsInAnyOrder(GROUP_KEY, "product group"));
    }

    @Test
    public void userIsCreatedWithDefaultProductGroupsOneOrMoreExpected() throws ConnectAddOnUserInitException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException, MembershipAlreadyExistsException
    {
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsAlwaysExpected()).thenReturn(Collections.<String>emptySet());
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected()).thenReturn(ImmutableSet.of("product group"));
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService, times(2)).addUserToGroup(eq(application), eq(USER_KEY), captor.capture());
        assertThat(captor.getAllValues(), containsInAnyOrder(GROUP_KEY, "product group"));
    }

    @Test
    public void userIsCreatedWithDefaultProductGroupsOneOrMoreExpectedAndAlwaysExpected() throws ConnectAddOnUserInitException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException, MembershipAlreadyExistsException
    {
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsAlwaysExpected()).thenReturn(ImmutableSet.of("product group 1"));
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected()).thenReturn(ImmutableSet.of("product group 2"));
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService, times(3)).addUserToGroup(eq(application), eq(USER_KEY), captor.capture());
        assertThat(captor.getAllValues(), containsInAnyOrder(GROUP_KEY, "product group 1", "product group 2"));
    }

    @Test
    public void userIsEnabledWithAtlassianConnectUserAttribute()
            throws Exception
    {
        when(applicationService.findUserByName(application, USER_KEY)).thenReturn(user);
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);

        verify(applicationService).storeUserAttributes(eq(application), eq(USER_KEY), attributeCalled(buildAttributeConnectAddOnAttributeName("app-name")));
        verify(crowdClient).storeUserAttributes(eq(USER_KEY), attributeCalled(buildAttributeConnectAddOnAttributeName("app-name")));
    }

    @Test
    public void userIsEnabledWithAtlassianConnectUserAttributeWhenNotInOnDemand()
            throws Exception
    {
        when(featureManager.isOnDemand()).thenReturn(false);
        when(applicationService.findUserByName(application, USER_KEY)).thenReturn(user);
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);

        verify(applicationService).storeUserAttributes(eq(application), eq(USER_KEY), attributeCalled(buildAttributeConnectAddOnAttributeName("app-name")));
        verify(crowdClient, never()).storeUserAttributes(anyString(), anyMap());
    }


    @Test
    public void userIsCreatedWithAtlassianConnectUserAttribute()
            throws InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException, UserNotFoundException, InvalidAuthenticationException
    {
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService).addUser(eq(application), argThat(hasExpectedEmailAddress()), any(PasswordCredential.class));
        verify(applicationService).storeUserAttributes(eq(application), eq(USER_KEY), attributeCalled(buildAttributeConnectAddOnAttributeName("app-name")));
        verify(crowdClient).storeUserAttributes(eq(USER_KEY), attributeCalled(buildAttributeConnectAddOnAttributeName("app-name")));
    }

    @Test
    public void userIsCreatedWithAtlassianConnectUserAttributeWhenNotInOnDemand()
            throws InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException, UserNotFoundException, InvalidAuthenticationException
    {
        when(featureManager.isOnDemand()).thenReturn(false);

        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService).addUser(eq(application), argThat(hasExpectedEmailAddress()), any(PasswordCredential.class));
        verify(applicationService).storeUserAttributes(eq(application), eq(USER_KEY), attributeCalled(buildAttributeConnectAddOnAttributeName("app-name")));
        verify(crowdClient, never()).storeUserAttributes(anyString(), anyMap());
    }

    @Test
    public void userIsAddedToDefiniteDefaultProductGroupsIfItExistedAndWasNotAMember() throws UserNotFoundException, InvalidUserException, InvalidCredentialException, ApplicationPermissionException, OperationFailedException, ConnectAddOnUserInitException, GroupNotFoundException, MembershipAlreadyExistsException
    {
        theUserExists();
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsAlwaysExpected()).thenReturn(ImmutableSet.of("product group"));
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected()).thenReturn(Collections.<String>emptySet());
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService, times(2)).addUserToGroup(eq(application), eq(USER_KEY), captor.capture());
        assertThat(captor.getAllValues(), containsInAnyOrder(GROUP_KEY, "product group"));
    }

    @Test
    public void userIsAddedToPossibleDefaultProductGroupsIfItExistedAndWasNotAMember() throws UserNotFoundException, InvalidUserException, InvalidCredentialException, ApplicationPermissionException, OperationFailedException, ConnectAddOnUserInitException, GroupNotFoundException, MembershipAlreadyExistsException
    {
        theUserExists();
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsAlwaysExpected()).thenReturn(Collections.<String>emptySet());
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected()).thenReturn(ImmutableSet.of("product group"));
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService, times(2)).addUserToGroup(eq(application), eq(USER_KEY), captor.capture());
        assertThat(captor.getAllValues(), containsInAnyOrder(GROUP_KEY, "product group"));
    }

    @Test
    public void userIsNotAddedToDefiniteDefaultProductGroupsIfItWasAlreadyAMember() throws UserNotFoundException, InvalidUserException, InvalidCredentialException, ApplicationPermissionException, OperationFailedException, ConnectAddOnUserInitException, GroupNotFoundException, MembershipAlreadyExistsException
    {
        theUserExists();
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsAlwaysExpected()).thenReturn(ImmutableSet.of("product group"));
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected()).thenReturn(Collections.<String>emptySet());
        when(applicationService.isUserDirectGroupMember(eq(application), eq(USER_KEY), eq("product group"))).thenReturn(true);
        connectAddOnUserService.getOrCreateUserKey(ADD_ON_KEY, ADD_ON_DISPLAY_NAME);
        verify(applicationService, never()).addUserToGroup(eq(application), eq(USER_KEY), eq("product group"));
    }

    @Test
    public void userIsNotAddedToPossibleDefaultProductGroupsIfItWasAlreadyAMember() throws UserNotFoundException, InvalidUserException, InvalidCredentialException, ApplicationPermissionException, OperationFailedException, ConnectAddOnUserInitException, GroupNotFoundException, MembershipAlreadyExistsException
    {
        theUserExists();
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsAlwaysExpected()).thenReturn(Collections.<String>emptySet());
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected()).thenReturn(ImmutableSet.of("product group"));
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
        when(crowdClientFacade.getClientApplicationName()).thenReturn("app-name");
        when(applicationService.addUser(eq(application), eq(new UserTemplate(USER_KEY)), eq(PasswordCredential.NONE))).thenReturn(user);
        when(user.getName()).thenReturn(USER_KEY);
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsAlwaysExpected()).thenReturn(Collections.<String>emptySet());
        when(connectAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected()).thenReturn(Collections.<String>emptySet());
        when(crowdClientFacade.getCrowdClient()).thenReturn(crowdClient);
        when(featureManager.isOnDemand()).thenReturn(true);
        ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService = new ConnectAddOnUserGroupProvisioningServiceImpl(applicationService, applicationManager);
        connectAddOnUserService = new ConnectAddOnUserServiceImpl(applicationService, applicationManager, connectAddOnUserProvisioningService, connectAddOnUserGroupProvisioningService, featureManager, crowdClientFacade);
    }

    @SuppressWarnings ("unchecked")
    private static Map<String, Set<String>> attributeCalled(String attributeName)
    {
        return (Map<String, Set<String>>) argThat(hasEntry(attributeName, EXPECTED_ATTRIBUTE_VALUE));
    }
}
