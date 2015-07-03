package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.plugin.connect.crowd.usermanagement.api.ConnectCrowdException;
import com.atlassian.plugin.connect.crowd.usermanagement.api.ConnectCrowdService;

public abstract class ConnectCrowdServiceBase implements ConnectCrowdService
{
    private final UserReconciliation userReconciliation;

    public ConnectCrowdServiceBase(UserReconciliation userReconciliation)
    {
        this.userReconciliation = userReconciliation;
    }

    @Override
    public User createOrEnableUser(String username, String displayName, String emailAddress, PasswordCredential passwordCredential) throws ConnectCrowdException
    {
        Optional<? extends User> user = findUserByName(username);
        if (!user.isPresent())
        {
            return createUser(username, displayName, emailAddress, passwordCredential);
        }

        User foundUser = user.get();
        Optional<UserTemplate> requiredUpdates = userReconciliation.getFixes(foundUser, displayName, emailAddress, true);
        if (requiredUpdates.isPresent())
        {
            updateUser(requiredUpdates.get());
        }
        return foundUser;
    }

    @Override
    public void disableUser(String username) throws ConnectCrowdException
    {
        Optional<? extends User> user = findUserByName(username);
        if (user.isPresent())
        {
            UserTemplate userTemplate = new UserTemplate(user.get());
            updateUser(userTemplate);
        }
    }

    @Override
    public abstract void setAttributesOnUser(User user, Map<String, Set<String>> attributes)
        throws ConnectCrowdException;

    private User createUser(String username, String displayName, String emailAddress, PasswordCredential passwordCredential)
            throws ConnectCrowdException
    {
        UserTemplate userTemplate = new UserTemplate(username);
        userTemplate.setEmailAddress(emailAddress);
        userTemplate.setActive(true); //if you don't set this, it defaults to inactive!!!
        userTemplate.setDisplayName(displayName);

        try
        {
            addUser(userTemplate, passwordCredential);
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

        Optional<? extends User> user = findUserByName(username);
        if (!user.isPresent())
        {
            throw new ConnectCrowdException();
        }

        return user.get();
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

    protected abstract void addUser(UserTemplate userTemplate, PasswordCredential passwordCredential)
            throws ConnectCrowdException, OperationFailedException, InvalidUserException;

    protected abstract void updateUser(UserTemplate fixes) throws ConnectCrowdException;

    protected abstract Optional<? extends User> findUserByName(String username);
}
