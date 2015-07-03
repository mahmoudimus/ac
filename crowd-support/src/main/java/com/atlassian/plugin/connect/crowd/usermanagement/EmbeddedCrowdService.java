package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.plugin.connect.crowd.usermanagement.api.ConnectCrowdException;

public class EmbeddedCrowdService extends ConnectCrowdServiceBase
{
    private static final String CROWD_APPLICATION_NAME = "crowd-embedded"; // magic knowledge
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
            throws ConnectCrowdException
    {
        try
        {
            applicationService.storeUserAttributes(application(), user.getName(), attributes);
        }
        catch (OperationFailedException | UserNotFoundException | ApplicationPermissionException e)
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
            applicationService.addUser(application(), userTemplate, passwordCredential);
        }
        catch (InvalidCredentialException | ApplicationPermissionException e)
        {
            throw new ConnectCrowdException(e);
        }
    }

    protected void updateUser(UserTemplate requiredUpdates)
            throws ConnectCrowdException
    {
        try
        {
            applicationService.updateUser(application(), requiredUpdates);
        }
        catch (InvalidUserException | ApplicationPermissionException
                | OperationFailedException | UserNotFoundException e)
        {
            throw new ConnectCrowdException(e);
        }
    }

    protected Optional<? extends User> findUserByName(String username)
    {
        try
        {
            return Optional.ofNullable(applicationService.findUserByName(application(), username));
        }
        catch (UserNotFoundException e)
        {
            return Optional.empty();
        }
    }

    // Richard Atkins says that the Application is immutable and therefore the instance replaced every time changes occur,
    // and that therefore we should never cache it
    private Application application()
    {
        try
        {
            return applicationManager.findByName(CROWD_APPLICATION_NAME);
        }
        catch (ApplicationNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
}
