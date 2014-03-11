package com.atlassian.plugin.connect.plugin.usermanagement;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserUtil.Constants;
import static com.google.common.base.Preconditions.checkNotNull;

@ExportAsDevService
@Component
public class ConnectAddOnUserServiceImpl implements ConnectAddOnUserService
{
    private final ApplicationService applicationService;
    private final ApplicationManager applicationManager;
    private final ConnectAddOnUserProvisioningService connectAddOnUserProvisioningService;
    private final ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService;

    private static final Logger log = LoggerFactory.getLogger(ConnectAddOnUserServiceImpl.class);

    @Autowired
    public ConnectAddOnUserServiceImpl(ApplicationService applicationService,
                                       ApplicationManager applicationManager,
                                       ConnectAddOnUserProvisioningService connectAddOnUserProvisioningService,
                                       ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService)
    {
        this.applicationService = checkNotNull(applicationService);
        this.applicationManager= checkNotNull(applicationManager);
        this.connectAddOnUserProvisioningService = checkNotNull(connectAddOnUserProvisioningService);
        this.connectAddOnUserGroupProvisioningService = checkNotNull(connectAddOnUserGroupProvisioningService);
    }

    @Override
    public String getOrCreateUserKey(String addOnKey) throws ConnectAddOnUserInitException
    {
        // Oh how I long for Java 7's conciser catch semantics.
        try
        {
            return createOrEnableAddOnUser(ConnectAddOnUserUtil.usernameForAddon(addOnKey));
        }
        catch (InvalidCredentialException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
        catch (InvalidUserException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
        catch (ApplicationPermissionException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
        catch (OperationFailedException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
        catch (MembershipAlreadyExistsException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
        catch (InvalidGroupException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
        catch (GroupNotFoundException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
        catch (UserNotFoundException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
        catch (ApplicationNotFoundException e)
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
                catch (InvalidUserException e)
                {
                    throw new ConnectAddOnUserDisableException(e);
                }
                catch (OperationFailedException e)
                {
                    throw new ConnectAddOnUserDisableException(e);
                }
                catch (ApplicationPermissionException e)
                {
                    throw new ConnectAddOnUserDisableException(e);
                }
                catch (UserNotFoundException e)
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
    public String provisionAddonUserForScopes(String addOnKey, Set<ScopeName> previousScopes, Set<ScopeName> newScopes) throws ConnectAddOnUserInitException
    {
        String username = getOrCreateUserKey(addOnKey);
        connectAddOnUserProvisioningService.provisionAddonUserForScopes(username, previousScopes, newScopes);
        return username;
    }

    private String createOrEnableAddOnUser(String username) throws InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException, MembershipAlreadyExistsException, InvalidGroupException, GroupNotFoundException, UserNotFoundException, ApplicationNotFoundException, ConnectAddOnUserInitException
    {
        connectAddOnUserGroupProvisioningService.ensureGroupExists(Constants.ADDON_USER_GROUP_KEY);
        User user = ensureUserExists(username);
        connectAddOnUserGroupProvisioningService.ensureUserIsInGroup(user.getName(), Constants.ADDON_USER_GROUP_KEY);

        for (String group : connectAddOnUserProvisioningService.getDefaultProductGroups())
        {
            try
            {
                connectAddOnUserGroupProvisioningService.ensureUserIsInGroup(user.getName(), group);
            }
            catch (GroupNotFoundException e)
            {
                // carry on if the group does not exist so that an admin deleting a group will not kill all add-on installations
                log.error(String.format("Could not make user '%s' a member of group '%s' because that group does not exist!", username, group));
                // TODO ACDEV-938: propagate this error
            }
        }

        return user.getName();
    }

    private User ensureUserExists(String username) throws OperationFailedException, InvalidCredentialException, ApplicationPermissionException, UserNotFoundException, InvalidUserException, ApplicationNotFoundException, ConnectAddOnUserInitException
    {
        User user = findUserByUsername(username);

        if (null == user)
        {
            user = createUser(username);
        }
        else
        {
            // just in case an admin changes the email address
            // (we don't rely on this to prevent an admin taking control of the account, but it would make it more difficult)
            if (!Constants.ADDON_USER_EMAIL_ADDRESS.equals(user.getEmailAddress()) || !user.isActive())
            {
                UserTemplate userTemplate = new UserTemplate(user);
                userTemplate.setEmailAddress(Constants.ADDON_USER_EMAIL_ADDRESS);
                userTemplate.setActive(true);
                applicationService.updateUser(getApplication(), userTemplate);
            }

            // Justin Koke says that NONE password prevents logging in
            applicationService.updateUserCredential(getApplication(), user.getName(), PasswordCredential.NONE);
        }

        return user;
    }

    private User createUser(String username) throws OperationFailedException, InvalidCredentialException, ApplicationPermissionException, ApplicationNotFoundException, ConnectAddOnUserInitException
    {
        User user;
        try
        {
            // Justin Koke says that NONE password prevents logging in
            UserTemplate userTemplate = new UserTemplate(username);
            userTemplate.setEmailAddress(Constants.ADDON_USER_EMAIL_ADDRESS); // so that "reset password" emails go nowhere
            userTemplate.setActive(true); //if you don't set this, it defaults to inactive!!!
            user = applicationService.addUser(getApplication(), userTemplate, PasswordCredential.NONE);

            if (null == user)
            {
                throw new ConnectAddOnUserInitException(String.format("Tried to create user '%s' but the %s returned a null user!", username, applicationService.getClass().getSimpleName()));
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

        return user;
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
