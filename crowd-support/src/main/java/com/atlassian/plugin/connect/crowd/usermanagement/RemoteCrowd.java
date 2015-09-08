package com.atlassian.plugin.connect.crowd.usermanagement;

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
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class RemoteCrowd extends ConnectCrowdBase
{
    private static final Logger log = LoggerFactory.getLogger(RemoteCrowd.class);
    private final CrowdClientProvider crowdClientProvider;

    public RemoteCrowd(CrowdClientProvider crowdClientProvider, UserReconciliation userReconciliation)
    {
        super(userReconciliation);
        this.crowdClientProvider = crowdClientProvider;
    }

    @Override
    public void setAttributesOnUser(String username, Map<String, Set<String>> attributes)
    {
        try
        {
            client().storeUserAttributes(username, attributes);
        }
        catch (UserNotFoundException | InvalidAuthenticationException | ApplicationPermissionException | OperationFailedException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
    }

    @Override
    protected void addUser(UserTemplate userTemplate, PasswordCredential passwordCredential)
            throws OperationFailedException, InvalidUserException
    {
        try
        {
            client().addUser(userTemplate, passwordCredential);
        }
        catch (InvalidCredentialException | ApplicationPermissionException | InvalidAuthenticationException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
    }

    protected void updateUser(UserTemplate requiredUpdates)
    {
        try
        {
            client().updateUser(requiredUpdates);
        }
        catch (InvalidUserException | InvalidAuthenticationException
                | ApplicationPermissionException | OperationFailedException | UserNotFoundException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
    }
    
    protected void updateUserCredential(String username, PasswordCredential passwordCredential)
    {
        try
        {
            if (passwordCredential.equals(PasswordCredential.NONE))
            {
                client().updateUserCredential(username, null);
            }
            else
            {
                client().updateUserCredential(username, passwordCredential.getCredential());
            }
        }
        catch (UserNotFoundException | InvalidAuthenticationException |
                ApplicationPermissionException | OperationFailedException | InvalidCredentialException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
    }

    public Optional<? extends User> findUserByName(String username)
    {
        try
        {
            return Optional.of(client().getUser(username));
        }
        catch (UserNotFoundException | InvalidAuthenticationException |
                ApplicationPermissionException | OperationFailedException e)
        {
            return Optional.absent();
        }
    }

    @Override
    protected void addGroup(String groupName)
            throws InvalidGroupException, OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException
    {
        client().addGroup(new GroupTemplate(groupName));
    }

    private CrowdClient client()
    {
        return crowdClientProvider.getCrowdClient();
    }

    @Override
    public void ensureUserIsInGroup(String username, String groupName)
            throws ApplicationNotFoundException, UserNotFoundException,
            ApplicationPermissionException, GroupNotFoundException,
            OperationFailedException, InvalidAuthenticationException
    {
        log.info("Attempting to make user '{}' a member of group '{}' (if not already a member).", username, groupName);

        if (!client().isUserDirectGroupMember(username, groupName))
        {
            try
            {
                client().addUserToGroup(username, groupName);
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
            ApplicationPermissionException, GroupNotFoundException, OperationFailedException,
            InvalidAuthenticationException
    {
        try
        {
            client().removeUserFromGroup(username, groupName);
            log.info("Removed user '{}' from group '{}'.", username, groupName);
        }
        catch (MembershipNotFoundException e)
        {
            // ignore, we wanted to remove the member anyway
        }
    }

    @Override
    public Group findGroupByKey(String groupName)
            throws ApplicationNotFoundException, ApplicationPermissionException, InvalidAuthenticationException
    {
        Group group;
        try
        {
            group = client().getGroup(groupName);
        }
        catch (GroupNotFoundException| OperationFailedException gnf)
        {
            group = null;
        }
        return group;
    }

    @Override
    public void invalidateSessions(String username) throws OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException
    {
        client().invalidateSSOTokensForUser(username);
    }
}
