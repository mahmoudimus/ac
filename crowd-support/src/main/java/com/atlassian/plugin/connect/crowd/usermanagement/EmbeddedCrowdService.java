package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.MembershipAlreadyExistsException;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedCrowdService extends ConnectCrowdServiceBase
{
    private static final Logger log = LoggerFactory.getLogger(EmbeddedCrowdService.class);
    private final ApplicationService applicationService;
    private ApplicationManager applicationManager;

    public EmbeddedCrowdService(ApplicationService applicationService, UserReconciliation userReconciliation, ApplicationManager applicationManager)
    {
        super(userReconciliation);
        this.applicationService = applicationService;
        this.applicationManager = applicationManager;
    }

    @Override
    public void setAttributesOnUser(User user, Map<String, Set<String>> attributes)
    {
        try
        {
            applicationService.storeUserAttributes(getCrowdApplication(), user.getName(), attributes);
        }
        catch (OperationFailedException | UserNotFoundException | ApplicationPermissionException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
    }

    @Override
    public void ensureUserIsInGroup(String username, String groupName)
            throws ApplicationNotFoundException, UserNotFoundException,
            ApplicationPermissionException, GroupNotFoundException, OperationFailedException
    {
        log.info("Attempting to make user '{}' a member of group '{}' (if not already a member).", username, groupName);

        if (!applicationService.isUserDirectGroupMember(getCrowdApplication(), username, groupName))
        {
            try
            {
                applicationService.addUserToGroup(getCrowdApplication(), username, groupName);
                log.info("Added user '{}' to group '{}',", username, groupName);
            }
            catch (MembershipAlreadyExistsException e)
            {
                // ignore, because the membership that we're trying to create exists
            }
        }
    }

    @Override
    public void removeUserFromGroup(String username, String groupName)
            throws ApplicationNotFoundException, UserNotFoundException,
            ApplicationPermissionException, GroupNotFoundException, OperationFailedException
    {
        try
        {
            applicationService.removeUserFromGroup(getCrowdApplication(), username, groupName);
            log.info("Removed user '{}' from group '{}'.", username, groupName);
        }
        catch (MembershipNotFoundException e)
        {
            // ignore, we wanted to remove the member anyway
        }
    }

    @Override
    public boolean ensureGroupExists(String groupName)
            throws ApplicationNotFoundException, ApplicationPermissionException,
            OperationFailedException
    {
        boolean created = false;

        if (null == findGroupByKey(groupName))
        {
            try
            {
                applicationService.addGroup(getCrowdApplication(), new GroupTemplate(groupName));
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
                    throw new RuntimeException(String.format("The %s %s said that the %s '%s' did not exist, then that it could not be created because it does exist, then that it does not exist. Find a Crowd coder and beat them over the head with this message.",
                            ApplicationService.class.getSimpleName(), applicationService, Group.class.getSimpleName(), groupName));
                }
            }
        }

        return created;
    }

    @Override
    public Group findGroupByKey(String groupName) throws ApplicationNotFoundException
    {
        Group group;
        try
        {
            group = applicationService.findGroupByName(getCrowdApplication(), groupName);
        }
        catch (GroupNotFoundException gnf)
        {
            group = null;
        }
        return group;
    }

    @Override
    protected void addUser(UserTemplate userTemplate, PasswordCredential passwordCredential)
            throws OperationFailedException, InvalidUserException
    {
        try
        {
            applicationService.addUser(getCrowdApplication(), userTemplate, passwordCredential);
        }
        catch (InvalidCredentialException | ApplicationPermissionException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
    }

    protected void updateUser(UserTemplate requiredUpdates)
    {
        try
        {
            applicationService.updateUser(getCrowdApplication(), requiredUpdates);
        }
        catch (InvalidUserException | ApplicationPermissionException
                | OperationFailedException | UserNotFoundException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
    }

    protected Optional<? extends User> findUserByName(String username)
    {
        try
        {
            return Optional.ofNullable(applicationService.findUserByName(getCrowdApplication(), username));
        }
        catch (UserNotFoundException e)
        {
            return Optional.empty();
        }
    }

    // Richard Atkins says that the Application is immutable and therefore the instance replaced every time changes occur,
    // and that therefore we should never cache it
    @Override
    public Application getCrowdApplication()
    {
        try
        {
            return applicationManager.findByName(getCrowdApplicationName());
        }
        catch (ApplicationNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
}
