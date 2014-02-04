package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.user.UserTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class ConnectAddOnUserServiceImpl implements ConnectAddOnUserService
{
    private final ApplicationService applicationService;
    private final Application application;

    private static final String ADD_ON_USER_KEY_PREFIX = "addon_";
    private static final String ATLASSIAN_CONNECT_ADD_ONS_USER_GROUP_KEY = "atlassian-addons"; // in order to not occupy a license this has to match constant in user-provisioning-plugin/src/main/java/com/atlassian/crowd/plugin/usermanagement/userprovisioning/Constants.java
    private static final String CROWD_APPLICATION_NAME = "crowd-embedded"; // magic knowledge

    // Use a "no reply" email address for add-on users so that
    //   * reset password attempts are not received by anyone, and
    //   * there are no error messages in logs about failing to email.
    // Note that an admin can still change the email address but that non-admins can't simply click a "I lost my password" link and take control of the account.
    // We also rely on the user-provisioning-plugin to count add-on users as consuming licenses if an admin does take control of an account and use it to log in.
    // The rationale is that either they can't log in as these users, in which case they consume no licenses, or logging in is possible and such users do consume licenses.
    private static final String NO_REPLY_EMAIL_ADDRESS = "noreply@mailer.atlassian.com";

    @Autowired
    public ConnectAddOnUserServiceImpl(ApplicationService applicationService, ApplicationManager applicationManager) throws ApplicationNotFoundException
    {
        this.applicationService = checkNotNull(applicationService);
        this.application = checkNotNull(applicationManager.findByName(CROWD_APPLICATION_NAME));
    }

    @Override
    public String getOrCreateUserKey(String addOnKey) throws ConnectAddOnUserInitException
    {
        // Oh how I long for Java 7's conciser catch semantics.
        try
        {
            return createOrEnableAddOnUser(ADD_ON_USER_KEY_PREFIX + addOnKey).getName();
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
    }

    private User createOrEnableAddOnUser(String userKey) throws InvalidCredentialException, InvalidUserException, ApplicationPermissionException, OperationFailedException, MembershipAlreadyExistsException, InvalidGroupException, GroupNotFoundException, UserNotFoundException
    {
        ensureGroupExists();
        User user = ensureUserExists(userKey);
        ensureUserIsInGroup(userKey);

        // TODO ACDEV-933: enable user if disabled

        return user;
    }

    private void ensureUserIsInGroup(String userKey) throws OperationFailedException, UserNotFoundException, GroupNotFoundException, ApplicationPermissionException, MembershipAlreadyExistsException
    {
        if (!applicationService.isUserDirectGroupMember(application, userKey, ATLASSIAN_CONNECT_ADD_ONS_USER_GROUP_KEY))
        {
            try
            {
                applicationService.addUserToGroup(application, userKey, ATLASSIAN_CONNECT_ADD_ONS_USER_GROUP_KEY);
            }
            catch (MembershipAlreadyExistsException e)
            {
                // ignore, because the membership that we're trying to create exists
            }
        }
    }

    private User ensureUserExists(String userKey) throws OperationFailedException, InvalidCredentialException, ApplicationPermissionException, UserNotFoundException, InvalidUserException
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
            if (!NO_REPLY_EMAIL_ADDRESS.equals(user.getEmailAddress()))
            {
                UserTemplate userTemplate = new UserTemplate(user);
                userTemplate.setEmailAddress(NO_REPLY_EMAIL_ADDRESS);
                applicationService.updateUser(application, userTemplate);
            }
        }

        return user;
    }

    private User createUser(String userKey) throws OperationFailedException, InvalidCredentialException, ApplicationPermissionException
    {
        User user;
        try
        {
            // Justin Koke says that NONE password prevents logging in
            UserTemplate userTemplate = new UserTemplate(userKey);
            userTemplate.setEmailAddress(NO_REPLY_EMAIL_ADDRESS); // so that "reset password" emails go nowhere
            user = applicationService.addUser(application, userTemplate, PasswordCredential.NONE);
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

    private void ensureGroupExists() throws OperationFailedException, ApplicationPermissionException
    {
        if (null == findGroupByKey())
        {
            try
            {
                applicationService.addGroup(application, new GroupTemplate(ATLASSIAN_CONNECT_ADD_ONS_USER_GROUP_KEY));
            }
            catch (InvalidGroupException ige)
            {
                // according to its javadoc addGroup() throws InvalidGroupException if the group already exists
                // --> handle the race condition of something else creating this group at around the same time

                if (null == findGroupByKey())
                {
                    // the ApplicationService is messing us around by saying that the group exists and then that it does not
                    throw new RuntimeException(String.format("The %s %s said that the %s '%s' did not exist, then that it could not be created because it does exist, then that it does not exist. Find a Crowd coder and beat them over the head with this message.",
                            ApplicationService.class.getSimpleName(), applicationService, Group.class.getSimpleName(), ATLASSIAN_CONNECT_ADD_ONS_USER_GROUP_KEY));
                }
            }
        }
    }

    private User findUserByKey(String userKey)
    {
        User user;
        try
        {
            user = applicationService.findUserByName(application, userKey);
        }
        catch (UserNotFoundException e)
        {
            user = null;
        }
        return user;
    }

    private Group findGroupByKey()
    {
        Group group;
        try
        {
            group = applicationService.findGroupByName(application, ATLASSIAN_CONNECT_ADD_ONS_USER_GROUP_KEY);
        }
        catch (GroupNotFoundException gnf)
        {
            group = null;
        }
        return group;
    }
}
