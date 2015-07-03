package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.plugin.connect.crowd.usermanagement.api.ConnectCrowdException;
import com.atlassian.plugin.connect.crowd.usermanagement.api.CrowdClientProvider;

public class RemoteCrowdService extends ConnectCrowdServiceBase
{
    private final CrowdClientProvider crowdClientProvider;

    public RemoteCrowdService(CrowdClientProvider crowdClientProvider, UserReconciliation userReconciliation)
    {
        super(userReconciliation);
        this.crowdClientProvider = crowdClientProvider;
    }

    @Override
    public void setAttributesOnUser(User user, Map<String, Set<String>> attributes)
            throws ConnectCrowdException
    {
        try
        {
            client().storeUserAttributes(user.getName(), attributes);
        }
        catch (UserNotFoundException | InvalidAuthenticationException | ApplicationPermissionException | OperationFailedException e)
        {
            throw new ConnectCrowdException(e);
        }
    }

    @Override
    protected void addUser(UserTemplate userTemplate, PasswordCredential passwordCredential)
            throws ConnectCrowdException, OperationFailedException, InvalidUserException
    {
        try
        {
            client().addUser(userTemplate, passwordCredential);
        }
        catch (InvalidCredentialException | ApplicationPermissionException | InvalidAuthenticationException e)
        {
            throw new ConnectCrowdException(e);
        }
    }

    protected void updateUser(UserTemplate requiredUpdates)
            throws ConnectCrowdException
    {
        try
        {
            client().updateUser(requiredUpdates);
        }
        catch (InvalidUserException | InvalidAuthenticationException
                | ApplicationPermissionException | OperationFailedException | UserNotFoundException e)
        {
            throw new ConnectCrowdException(e);
        }
    }

    protected Optional<? extends User> findUserByName(String username)
    {
        try
        {
            return Optional.of(client().getUser(username));
        }
        catch (UserNotFoundException | InvalidAuthenticationException |
                ApplicationPermissionException | OperationFailedException e)
        {
            return Optional.empty();
        }
    }

    private CrowdClient client()
    {
        return crowdClientProvider.getCrowdClient();
    }
}
