package com.atlassian.plugin.connect.core.usermanagement;

import java.util.Set;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.MembershipAlreadyExistsException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserProvisioningService;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.product.FeatureManager;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil.Constants;
import static com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserUtil.buildConnectAddOnUserAttribute;
import static com.google.common.base.Preconditions.checkNotNull;

@ExportAsDevService
@Component
public class ConnectAddOnUserServiceImpl implements ConnectAddOnUserService
{
    private final ApplicationService applicationService;
    private final ApplicationManager applicationManager;
    private final ConnectAddOnUserProvisioningService connectAddOnUserProvisioningService;
    private final ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService;
    private final FeatureManager featureManager;
    private static final Logger log = LoggerFactory.getLogger(ConnectAddOnUserServiceImpl.class);
    private final CrowdClientFacade crowdClientFacade;

    @Autowired
    public ConnectAddOnUserServiceImpl(ApplicationService applicationService,
            ApplicationManager applicationManager,
            ConnectAddOnUserProvisioningService connectAddOnUserProvisioningService,
            ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService, FeatureManager featureManager, CrowdClientFacade crowdClientFacade)
    {
        this.crowdClientFacade = crowdClientFacade;
        this.applicationService = checkNotNull(applicationService);
        this.applicationManager= checkNotNull(applicationManager);
        this.connectAddOnUserProvisioningService = checkNotNull(connectAddOnUserProvisioningService);
        this.connectAddOnUserGroupProvisioningService = checkNotNull(connectAddOnUserGroupProvisioningService);
        this.featureManager = checkNotNull(featureManager);
    }

    @Override
    public String getOrCreateUserKey(String addOnKey, String addOnDisplayName) throws ConnectAddOnUserInitException
    {
        try
        {
            return createOrEnableAddOnUser(ConnectAddOnUserUtil.usernameForAddon(checkNotNull(addOnKey)), checkNotNull(addOnDisplayName));
        }
        catch (InvalidCredentialException
                | InvalidUserException
                | ApplicationPermissionException
                | MembershipAlreadyExistsException
                | InvalidGroupException
                | GroupNotFoundException
                | UserNotFoundException
                | ApplicationNotFoundException
                | InvalidAuthenticationException
                | OperationFailedException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
    }

    @Override
    public void disableAddonUser(String addOnKey) throws ConnectAddOnUserDisableException
    {
        String username = ConnectAddOnUserUtil.usernameForAddon(addOnKey);

        try
        {
            User user = findUserByUsername(username);

            if (null != user)
            {
                UserTemplate userTemplate = new UserTemplate(user);
                userTemplate.setActive(false);
                try
                {
                    applicationService.updateUser(getApplication(), userTemplate);
                }
                catch (InvalidUserException | OperationFailedException | ApplicationPermissionException | UserNotFoundException e)
                {
                    throw new ConnectAddOnUserDisableException(e);
                }
            }
        }
        catch (ApplicationNotFoundException e)
        {
            throw new ConnectAddOnUserDisableException(e);
        }
    }

    @VisibleForTesting
    @Override
    public boolean isAddOnUserActive(String addOnKey)
    {
        String username = ConnectAddOnUserUtil.usernameForAddon(addOnKey);
        User user;

        try
        {
            user = findUserByUsername(username);
        }
        catch (ApplicationNotFoundException e)
        {
            throw new IllegalStateException(e);
        }

        return null != user && user.isActive();
    }

    @Override
    public String provisionAddonUserForScopes(String addOnKey, String addOnDisplayName, Set<ScopeName> previousScopes, Set<ScopeName> newScopes) throws ConnectAddOnUserInitException
    {
        String username = getOrCreateUserKey(checkNotNull(addOnKey), checkNotNull(addOnDisplayName));
        connectAddOnUserProvisioningService.provisionAddonUserForScopes(username, previousScopes, newScopes);
        return username;
    }

    private String createOrEnableAddOnUser(String username, String addOnDisplayName)
            throws InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException, MembershipAlreadyExistsException, InvalidGroupException, GroupNotFoundException, UserNotFoundException, ApplicationNotFoundException, ConnectAddOnUserInitException, InvalidAuthenticationException
    {
        connectAddOnUserGroupProvisioningService.ensureGroupExists(Constants.ADDON_USER_GROUP_KEY);
        User user = ensureUserExists(username, addOnDisplayName);
        connectAddOnUserGroupProvisioningService.ensureUserIsInGroup(user.getName(), Constants.ADDON_USER_GROUP_KEY);



        for (String group : connectAddOnUserProvisioningService.getDefaultProductGroupsAlwaysExpected())
        {
            try
            {
                connectAddOnUserGroupProvisioningService.ensureUserIsInGroup(user.getName(), group);
            }
            catch (GroupNotFoundException e)
            {
                // carry on if the group does not exist so that an admin deleting a group will not kill all add-on installations
                log.error(String.format("Could not make user '%s' a member of group '%s' because that group does not exist! " +
                        "The user needs to be a member of this group, otherwise the add-on will not function correctly. " +
                        "Please check with an instance administrator that this group exists and that it is not read-only.", username, group));
                // TODO ACDEV-938: propagate this error
            }
        }

        int numPossibleDefaultGroupsAddedTo = 0;
        String errorMessage = String.format("Could not make user '%s' a member of one of groups ", username);
        for (String group : connectAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected())
        {
            try
            {
                connectAddOnUserGroupProvisioningService.ensureUserIsInGroup(user.getName(), group);
                numPossibleDefaultGroupsAddedTo++;
            }
            catch (GroupNotFoundException e)
            {
                errorMessage += String.format("%s, ", group);
            }

        }
        if (numPossibleDefaultGroupsAddedTo == 0 && connectAddOnUserProvisioningService.getDefaultProductGroupsOneOrMoreExpected().size() > 0)
        {
            log.error(errorMessage + "because none of those groups exist!" +
                    "We expect at least one of these groups to exist - exactly which one should exist depends on the version of the instance." +
                    "The user needs to be a member of one of these groups for basic access, otherwise the add-on will not function correctly." +
                    "Please check with an instance administrator that at least one of these groups exists and that it is not read-only.");
        }

        return user.getName();
    }

    private User ensureUserExists(String username, String addOnDisplayName)
            throws OperationFailedException, InvalidCredentialException, ApplicationPermissionException, UserNotFoundException, InvalidUserException, ApplicationNotFoundException, ConnectAddOnUserInitException, InvalidAuthenticationException
    {
        User user = findUserByUsername(username);

        if (null == user)
        {
            user = createUser(username, addOnDisplayName);
        }
        else
        {
            // just in case an admin changes the email address
            // (we don't rely on this to prevent an admin taking control of the account, but it would make it more difficult)
            if (!Constants.ADDON_USER_EMAIL_ADDRESS.equals(user.getEmailAddress()) || !user.isActive() || !addOnDisplayName.equals(user.getDisplayName()))
            {
                UserTemplate userTemplate = new UserTemplate(user);
                userTemplate.setEmailAddress(Constants.ADDON_USER_EMAIL_ADDRESS);
                userTemplate.setActive(true);
                userTemplate.setDisplayName(addOnDisplayName);
                applicationService.updateUser(getApplication(), userTemplate);
            }
        }

        addConnectUserAttribute(user);

        return user;
    }

    private User createUser(String username, String addOnDisplayName)
            throws OperationFailedException, InvalidCredentialException, ApplicationPermissionException, ApplicationNotFoundException, ConnectAddOnUserInitException, UserNotFoundException, InvalidAuthenticationException
    {
        User user;
        try
        {
            // Justin Koke says that NONE password prevents logging in
            UserTemplate userTemplate = new UserTemplate(username);
            userTemplate.setEmailAddress(Constants.ADDON_USER_EMAIL_ADDRESS); // so that "reset password" emails go nowhere
            userTemplate.setActive(true); //if you don't set this, it defaults to inactive!!!
            userTemplate.setDisplayName(addOnDisplayName);
            user = applicationService.addUser(getApplication(), userTemplate, PasswordCredential.NONE);

            if (null == user)
            {
                throw new ConnectAddOnUserInitException(String.format("Tried to create user '%s' but the %s returned a null user!",
                        username,
                        applicationService.getClass().getSimpleName()),
                        ConnectAddOnUserProvisioningService.USER_PROVISIONING_ERROR);
            }
            else
            {
                log.info("Created user '{}'", user.getName());
            }
        }
        catch (InvalidUserException iue)
        {
            // the javadoc says that addUser() throws an InvalidUserException if the user already exists
            // --> handle the race condition of something else creating this user at around the same time (as unlikely as that should be)
            user = findUserWithFastFailure(username, iue);
        }
        catch (OperationFailedException e)
        {
            // during Connect 1.0 blitz testing we observed this exception emanating from the bowels of Crowd, claiming that the user already exists
            // --> handle the race condition of something else creating this user at around the same time (as unlikely as that should be)
            user = findUserWithFastFailure(username, e);
        }
        catch (Exception e)
        {
            if (e.getClass().getCanonicalName().equals("org.springframework.dao.DataIntegrityViolationException"))
            {
                // our analytics revealed 57 of these in 1 week and prod instance logs suggest that this is another user creation race condition
                // see https://ecosystem.atlassian.net/browse/ACDEV-1499
                // --> handle the race condition of something else creating this user at around the same time (as unlikely as that should be)
                user = findUserWithFastFailure(username, e);
            }
            else
            {
                throw e;
            }
        }
        return user;
    }

    private void addConnectUserAttribute(User user)
            throws ApplicationNotFoundException, OperationFailedException, ApplicationPermissionException, UserNotFoundException, InvalidAuthenticationException
    {
        // Set connect attributes on user -- at this point we are confident we have a user
        ImmutableMap<String, Set<String>> connectAddOnUserAttribute = buildConnectAddOnUserAttribute(crowdClientFacade.getClientApplicationName());
        applicationService.storeUserAttributes(getApplication(), user.getName(), connectAddOnUserAttribute);

        if (featureManager.isOnDemand())
        {
            // Sets the connect attribute on the Remote Crowd Server if running in OD
            // This is currently required due to the fact that the DbCachingRemoteDirectory implementation used by JIRA and Confluence doesn't currently
            // write attributes back to the Crowd Server. This can be removed completely with Crowd 2.9 since addUser can take a UserWithAttributes in this version
            crowdClientFacade.getCrowdClient().storeUserAttributes(user.getName(), connectAddOnUserAttribute);
        }
    }

    private User findUserWithFastFailure(String username, Exception userAlreadyExistsException) throws ApplicationNotFoundException
    {
        final User user = findUserByUsername(username);

        if (null == user)
        {
            // the ApplicationService is messing us around by saying that the user exists and then that it does not
            throw new RuntimeException(String.format("The %s %s said that the %s '%s' did not exist, then that it could not be created because it does exist, then that it does not exist. Find a Crowd coder and beat them over the head with this message.",
                    ApplicationService.class.getSimpleName(), applicationService, User.class.getSimpleName(), username), userAlreadyExistsException);
        }

        return user;
    }

    private User findUserByUsername(String username) throws ApplicationNotFoundException
    {
        User user;
        try
        {
            user = applicationService.findUserByName(getApplication(), username);
        }
        catch (UserNotFoundException e)
        {
            user = null;
        }
        return user;
    }

    // Richard Atkins says that the Application is immutable and therefore the instance replaced every time changes occur,
    // and that therefore we should never cache it
    private Application getApplication() throws ApplicationNotFoundException
    {
        return applicationManager.findByName(connectAddOnUserGroupProvisioningService.getCrowdApplicationName());
    }
}

