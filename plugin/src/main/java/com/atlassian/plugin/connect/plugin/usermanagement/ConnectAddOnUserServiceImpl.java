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

import static com.google.common.base.Preconditions.checkNotNull;

@ExportAsDevService
@Component
public class ConnectAddOnUserServiceImpl implements ConnectAddOnUserService
{
    private final ApplicationService applicationService;
    private final ApplicationManager applicationManager;
    private final ConnectAddOnUserProvisioningService connectAddOnUserProvisioningService;
    private final ConnectAddOnUserGroupProvisioningService connectAddOnUserGroupProvisioningService;

    private static final String ADD_ON_USER_KEY_PREFIX = "addon_";
    private static final String ATLASSIAN_CONNECT_ADD_ONS_USER_GROUP_KEY = "atlassian-addons"; // in order to not occupy a license this has to match constant in user-provisioning-plugin/src/main/java/com/atlassian/crowd/plugin/usermanagement/userprovisioning/Constants.java

    // Use a "no reply" email address for add-on users so that
    //   * reset password attempts are not received by anyone, and
    //   * there are no error messages in logs about failing to email.
    // Note that an admin can still change the email address but that non-admins can't simply click a "I lost my password" link and take control of the account.
    // We also rely on the user-provisioning-plugin to count add-on users as consuming licenses if an admin does take control of an account and use it to log in.
    // The rationale is that either they can't log in as these users, in which case they consume no licenses, or logging in is possible and such users do consume licenses.
    private static final String NO_REPLY_EMAIL_ADDRESS = "noreply@mailer.atlassian.com";

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
            return createOrEnableAddOnUser(ADD_ON_USER_KEY_PREFIX + addOnKey);
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
        String userKey = ADD_ON_USER_KEY_PREFIX + addOnKey;

        try
        {
            User user = findUserByKey(userKey);

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
        String userKey = ADD_ON_USER_KEY_PREFIX + addOnKey;
        User user;

        try
        {
            user = findUserByKey(userKey);
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
        String userKey = getOrCreateUserKey(addOnKey);
        connectAddOnUserProvisioningService.provisionAddonUserForScopes(userKey, previousScopes, newScopes);
        return userKey;
    }

    private String createOrEnableAddOnUser(String userKey) throws InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException, MembershipAlreadyExistsException, InvalidGroupException, GroupNotFoundException, UserNotFoundException, ApplicationNotFoundException, ConnectAddOnUserInitException
    {
        connectAddOnUserGroupProvisioningService.ensureGroupExists(ATLASSIAN_CONNECT_ADD_ONS_USER_GROUP_KEY);
        User user = ensureUserExists(userKey);
        connectAddOnUserGroupProvisioningService.ensureUserIsInGroup(user.getName(), ATLASSIAN_CONNECT_ADD_ONS_USER_GROUP_KEY);

        for (String group : connectAddOnUserProvisioningService.getDefaultProductGroups())
        {
            try
            {
                connectAddOnUserGroupProvisioningService.ensureUserIsInGroup(user.getName(), group);
            }
            catch (GroupNotFoundException e)
            {
                // carry on if the group does not exist so that an admin deleting a group will not kill all add-on installations
                log.error(String.format("Could not make user '%s' a member of group '%s' because that group does not exist!", userKey, group), e);
                // TODO ACDEV-938: propagate this error
            }
        }

        return user.getName();
    }

    private User ensureUserExists(String userKey) throws OperationFailedException, InvalidCredentialException, ApplicationPermissionException, UserNotFoundException, InvalidUserException, ApplicationNotFoundException
    {
        User user = findUserByKey(userKey);

        if (null == user)
        {
            user = createUser(userKey);
        }
        else
        {
            // just in case an admin changes the email address
            // (we don't rely on this to prevent an admin taking control of the account, but it would make it more difficult)
            if (!NO_REPLY_EMAIL_ADDRESS.equals(user.getEmailAddress()) || !user.isActive())
            {
                UserTemplate userTemplate = new UserTemplate(user);
                userTemplate.setEmailAddress(NO_REPLY_EMAIL_ADDRESS);
                userTemplate.setActive(true);
                applicationService.updateUser(getApplication(), userTemplate);
            }

            // Justin Koke says that NONE password prevents logging in
            applicationService.updateUserCredential(getApplication(), user.getName(), PasswordCredential.NONE);
        }

        return user;
    }

    private User createUser(String userKey) throws OperationFailedException, InvalidCredentialException, ApplicationPermissionException, ApplicationNotFoundException
    {
        User user;
        try
        {
            // Justin Koke says that NONE password prevents logging in
            UserTemplate userTemplate = new UserTemplate(userKey);
            userTemplate.setEmailAddress(NO_REPLY_EMAIL_ADDRESS); // so that "reset password" emails go nowhere
            userTemplate.setActive(true); //if you don't set this, it defaults to inactive!!!
            user = applicationService.addUser(getApplication(), userTemplate, PasswordCredential.NONE);
        }
        catch (InvalidUserException iue)
        {
            // the javadoc says that addUser() throws an InvalidUserException if the user already exists
            // --> handle the race condition of something else creating this user at around the same time (as unlikely as that should be)

            user = findUserByKey(userKey);

            if (null == user)
            {
                // the ApplicationService is messing us around by saying that the user exists and then that it does not
                throw new RuntimeException(String.format("The %s %s said that the %s '%s' did not exist, then that it could not be created because it does exist, then that it does not exist. Find a Crowd coder and beat them over the head with this message.",
                        ApplicationService.class.getSimpleName(), applicationService, User.class.getSimpleName(), userKey));
            }
        }

        return user;
    }

    private User findUserByKey(String userKey) throws ApplicationNotFoundException
    {
        User user;
        try
        {
            user = applicationService.findUserByName(getApplication(), userKey);
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
