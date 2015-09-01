package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserProvisioningService;
import com.atlassian.plugin.connect.spi.user.ConnectAddOnUserDisableException;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.connect.crowd.usermanagement.UserCreationResult.UserNewness.NEWLY_CREATED;
import static com.atlassian.plugin.connect.crowd.usermanagement.UserCreationResult.UserNewness.PRE_EXISTING;

public abstract class ConnectCrowdBase
        implements ConnectAddOnUserGroupProvisioningService
{
    private final UserReconciliation userReconciliation;
    private static final Logger log = LoggerFactory.getLogger(ConnectCrowdBase.class);

    public ConnectCrowdBase(UserReconciliation userReconciliation)
    {
        this.userReconciliation = userReconciliation;
    }

    public UserCreationResult createOrEnableUser(String username, String displayName, String emailAddress, PasswordCredential passwordCredential)
    {
        Optional<? extends User> user = findUserByName(username);
        if (!user.isPresent())
        {
            return new UserCreationResult(createUser(username, displayName, emailAddress, passwordCredential), NEWLY_CREATED);
        }

        User foundUser = user.get();
        Optional<UserTemplate> requiredUpdates = userReconciliation.getFixes(foundUser, displayName, emailAddress, true);
        if (requiredUpdates.isPresent())
        {
            updateUser(requiredUpdates.get());
        }
        updateUserCredential(username, passwordCredential);
        return new UserCreationResult(foundUser, PRE_EXISTING);
    }

    public void disableUser(String username)
            throws ConnectAddOnUserDisableException
    {
        Optional<? extends User> user = findUserByName(username);
        if (user.isPresent())
        {
            UserTemplate userTemplate = new UserTemplate(user.get());
            try
            {
                userTemplate.setActive(false);
                updateUser(userTemplate);
            }
            catch (ConnectAddOnUserInitException e)
            {
                throw new ConnectAddOnUserDisableException((e.getCause() instanceof Exception) ?
                        (Exception) e.getCause() : null);
            }
        }
    }

    public abstract void setAttributesOnUser(String username, Map<String, Set<String>> attributes);

    private User createUser(String username, String displayName, String emailAddress, PasswordCredential passwordCredential)
    {
        UserTemplate userTemplate = new UserTemplate(username);
        userTemplate.setEmailAddress(emailAddress);
        userTemplate.setActive(true); //if you don't set this, it defaults to inactive!!!
        userTemplate.setDisplayName(displayName);

        try
        {
            addUser(userTemplate, passwordCredential);
            Optional<? extends User> user = findUserByName(username);
            if (!user.isPresent())
            {
                throw new ConnectAddOnUserInitException(String.format("Tried to create user '%s' but the %s returned a null user!",
                        username,
                        CrowdClient.class.getSimpleName()),
                        ConnectAddOnUserProvisioningService.USER_PROVISIONING_ERROR);
            }

            return user.get();
        }
        catch (OperationFailedException | InvalidUserException e)
        {
            // InvalidUserException:
            // the javadoc says that addUser() throws an InvalidUserException if the user already exists
            // --> handle the race condition of something else creating this user at around the same time (as unlikely as that should be)
            //
            // OperationFailedException
            // during Connect 1.0 blitz testing we observed this exception emanating from the bowels of Crowd, claiming that the user already exists
            // --> handle the race condition of something else creating this user at around the same time (as unlikely as that should be)
            return findUserWithFastFailure(username, e);
        }
        catch (Exception e)
        {
            // We can't put the actual class here because it is Spring-specific and may not be available in some products
            if (e.getClass().getCanonicalName().equals("org.springframework.dao.DataIntegrityViolationException"))
            {
                // DataIntegrityViolationException
                // the javadoc says that addUser() throws an InvalidUserException if the user already exists
                // --> handle the race condition of something else creating this user at around the same time (as unlikely as that should be)
                return findUserWithFastFailure(username, e);
            }
            else
            {
                throw e;
            }
        }
    }

    protected User findUserWithFastFailure(String username, Exception userAlreadyExistsException)
    {
        Optional<? extends User> user = findUserByName(username);

        if (!user.isPresent())
        {
            // the ApplicationService is messing us around by saying that the user exists and then that it does not
            throw new RuntimeException(
                    String.format("The crowd client said that the user '%s' did not exist, then that it could not be created because it does exist, then that it does not exist. Find a Crowd coder and beat them over the head with this message.", username),
                    userAlreadyExistsException);
        }

        return user.get();
    }

    public abstract Optional<? extends User> findUserByName(String username);

    protected abstract void addUser(UserTemplate userTemplate, PasswordCredential passwordCredential)
            throws OperationFailedException, InvalidUserException;

    protected abstract void updateUser(UserTemplate fixes);
    
    protected abstract void updateUserCredential(String username, PasswordCredential passwordCredential);

    protected abstract void addGroup(String groupName)
            throws InvalidGroupException, OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException;

    @Override
    public boolean ensureGroupExists(String groupName)
            throws ApplicationNotFoundException, ApplicationPermissionException,
            OperationFailedException, InvalidAuthenticationException
    {
        boolean created = false;

        if (null == findGroupByKey(groupName))
        {
            try
            {
                addGroup(groupName);
                created = true;
                log.info("Created group '{}'.", groupName);
            }
            catch (InvalidGroupException ige)
            {
                // according to its javadoc addGroup() throws InvalidGroupException if the group already exists
                // --> handle the race condition of something else creating this group at around the same time

                if (null == findGroupByKey(groupName))
                {
                    // the ApplicationService is messing us around by saying that the group exists and then that it does not
                    throw new RuntimeException(String.format("Crowd said that the %s '%s' did not exist, then that it could not be created because it does exist, then that it does not exist. Find a Crowd coder and beat them over the head with this message.",
                            Group.class.getSimpleName(), groupName));
                }
            }
        }

        return created;
    }
}
