package com.atlassian.plugin.connect.crowd.usermanagement;

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
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class EmbeddedCrowd extends ConnectCrowdBase
{
    private static final Logger log = LoggerFactory.getLogger(EmbeddedCrowd.class);
    private final ApplicationService applicationService;
    private final CrowdApplicationProvider crowdApplicationProvider;

    public EmbeddedCrowd(ApplicationService applicationService, UserReconciliation userReconciliation, CrowdApplicationProvider crowdApplicationProvider)
    {
        super(userReconciliation);
        this.applicationService = applicationService;
        this.crowdApplicationProvider = crowdApplicationProvider;
    }

    @Override
    public void setAttributesOnUser(String username, Map<String, Set<String>> attributes)
    {
        try
        {
            applicationService.storeUserAttributes(getCrowdApplication(), username, attributes);
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

    @Override
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
    
    @Override
    protected void updateUserCredential(String username, PasswordCredential passwordCredential)
    {
        try
        {
            applicationService.updateUserCredential(getCrowdApplication(), username, passwordCredential);
        }
        catch (InvalidCredentialException | ApplicationPermissionException
                | OperationFailedException | UserNotFoundException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
    }

    @Override
    public Optional<? extends User> findUserByName(String username)
    {
        try
        {
            return Optional.fromNullable(applicationService.findUserByName(getCrowdApplication(), username));
        }
        catch (UserNotFoundException e)
        {
            return Optional.absent();
        }
    }

    @Override
    protected void addGroup(String groupName)
            throws InvalidGroupException, OperationFailedException, ApplicationPermissionException
    {
        applicationService.addGroup(getCrowdApplication(), new GroupTemplate(groupName));
    }

    @Override
    public void invalidateSessions(String username)
    {
        throw new UnsupportedOperationException("Cannot invalidate session tokens in Embedded Crowd");
    }

    private Application getCrowdApplication()
    {
        try
        {
            return crowdApplicationProvider.getCrowdApplication();
        }
        catch (ApplicationNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }


}
