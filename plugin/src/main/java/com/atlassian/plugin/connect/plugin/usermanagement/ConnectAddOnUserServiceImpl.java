package com.atlassian.plugin.connect.plugin.usermanagement;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.MembershipAlreadyExistsException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
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

    private static final String ADD_ON_USER_KEY_PREFIX = "addon_";
    private static final String ATLASSIAN_CONNECT_ADD_ONS_USER_GROUP_KEY = "atlassian-addons"; // in order to not occupy a license this has to match constant in user-provisioning-plugin/src/main/java/com/atlassian/crowd/plugin/usermanagement/userprovisioning/Constants.java
    private static final String ATLASSIAN_ADDONS_ADMIN_GROUP_KEY = "atlassian-addons-admin";
    private static final String CROWD_APPLICATION_NAME = "crowd-embedded"; // magic knowledge

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
                                       ConnectAddOnUserProvisioningService connectAddOnUserProvisioningService)
    {
        this.applicationService = checkNotNull(applicationService);
        this.applicationManager= checkNotNull(applicationManager);
        this.connectAddOnUserProvisioningService = connectAddOnUserProvisioningService;
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
        User user = null;

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

    private String createOrEnableAddOnUser(String userKey) throws InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException, MembershipAlreadyExistsException, InvalidGroupException, GroupNotFoundException, UserNotFoundException, ApplicationNotFoundException, ConnectAddOnUserInitException
    {
        ensureGroupExists(ATLASSIAN_CONNECT_ADD_ONS_USER_GROUP_KEY);
        User user = ensureUserExists(userKey);
        ensureUserIsInGroup(user.getName(), ATLASSIAN_CONNECT_ADD_ONS_USER_GROUP_KEY);

        for (String group : connectAddOnUserProvisioningService.getDefaultProductGroups())
        {
            try
            {
                ensureUserIsInGroup(user.getName(), group);
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

    @Override
    public String provisionAddonUserForScopes(String addOnKey, Set<ScopeName> previousScopes, Set<ScopeName> newScopes) throws ConnectAddOnUserInitException
    {
        String userKey = getOrCreateUserKey(addOnKey);

        if (newScopes.contains(ScopeName.ADMIN) && !previousScopes.contains(ScopeName.ADMIN))
        {
            try
            {
                //TODO: Should this be shared between JIRA and Confluence?
                ensureGroupExistsAndIsAdmin(ATLASSIAN_ADDONS_ADMIN_GROUP_KEY);
                ensureUserIsInGroup(userKey, ATLASSIAN_ADDONS_ADMIN_GROUP_KEY);
            }
            catch (OperationFailedException e)
            {
                throw new ConnectAddOnUserInitException(e);
            }
            catch (ApplicationNotFoundException e)
            {
                throw new ConnectAddOnUserInitException(e);
            }
            catch (ApplicationPermissionException e)
            {
                throw new ConnectAddOnUserInitException(e);
            }
            catch (UserNotFoundException e)
            {
                throw new ConnectAddOnUserInitException(e);
            }
            catch (MembershipAlreadyExistsException e)
            {
                throw new ConnectAddOnUserInitException(e);
            }
            catch (GroupNotFoundException e)
            {
                throw new ConnectAddOnUserInitException(e);
            }
        }
        connectAddOnUserProvisioningService.provisionAddonUserForScopes(userKey, previousScopes, newScopes);
        return userKey;
    }

    private void ensureUserIsInGroup(String userKey, String groupKey) throws OperationFailedException, UserNotFoundException, GroupNotFoundException, ApplicationPermissionException, MembershipAlreadyExistsException, ApplicationNotFoundException
    {
        if (!applicationService.isUserDirectGroupMember(getApplication(), userKey, groupKey))
        {
            try
            {
                applicationService.addUserToGroup(getApplication(), userKey, groupKey);
            }
            catch (MembershipAlreadyExistsException e)
            {
                // ignore, because the membership that we're trying to create exists
            }
        }
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

    /**
     * Create the group if it does not exist. Do nothing if it previously existed.
     * @return {@code true} if the group previously existed, otherwise {@code false}
     */
    private boolean ensureGroupExists(String groupKey) throws OperationFailedException, ApplicationPermissionException, ApplicationNotFoundException
    {
        boolean created = false;

        if (null == findGroupByKey(groupKey))
        {
            try
            {
                applicationService.addGroup(getApplication(), new GroupTemplate(groupKey));
                created = true;
            }
            catch (InvalidGroupException ige)
            {
                // according to its javadoc addGroup() throws InvalidGroupException if the group already exists
                // --> handle the race condition of something else creating this group at around the same time

                if (null == findGroupByKey(groupKey))
                {
                    // the ApplicationService is messing us around by saying that the group exists and then that it does not
                    throw new RuntimeException(String.format("The %s %s said that the %s '%s' did not exist, then that it could not be created because it does exist, then that it does not exist. Find a Crowd coder and beat them over the head with this message.",
                            ApplicationService.class.getSimpleName(), applicationService, Group.class.getSimpleName(), groupKey));
                }
            }
        }

        return created;
    }

    private void ensureGroupExistsAndIsAdmin(String groupKey) throws ConnectAddOnUserInitException, OperationFailedException, ApplicationNotFoundException, ApplicationPermissionException
    {
        final boolean created = ensureGroupExists(groupKey);

        if (created)
        {
            connectAddOnUserProvisioningService.ensureGroupHasProductAdminPermission(groupKey);
        }
        else if (!connectAddOnUserProvisioningService.groupHasProductAdminPermission(groupKey))
        {
            throw new ConnectAddOnUserInitException(String.format("Group '%s' already exists and is NOT an administrators group. " +
                    "Cannot make it an administrators group because that would elevate the privileges of existing users in this group. " +
                    "Consequently, add-on users that need to be admins cannot be made admins by adding them to this group and making it an administrators group. " +
                    "Aborting user setup.",
                    groupKey));
        }
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

    private Group findGroupByKey(String groupKey) throws ApplicationNotFoundException
    {
        Group group;
        try
        {
            group = applicationService.findGroupByName(getApplication(), groupKey);
        }
        catch (GroupNotFoundException gnf)
        {
            group = null;
        }
        return group;
    }

    // Richard Atkins says that the Application is immutable and therefore the instance replaced every time changes occur,
    // and that therefore we should never cache it
    private Application getApplication() throws ApplicationNotFoundException
    {
        return applicationManager.findByName(CROWD_APPLICATION_NAME);
    }
}
