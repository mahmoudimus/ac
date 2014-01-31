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
        Group group;

        try
        {
            group = applicationService.findGroupByName(application, ATLASSIAN_CONNECT_ADD_ONS_USER_GROUP_KEY);
        }
        catch (GroupNotFoundException e)
        {
            group = null;
        }

        if (null == group)
        {
            applicationService.addGroup(application, new GroupTemplate(ATLASSIAN_CONNECT_ADD_ONS_USER_GROUP_KEY));
        }

        User user = null;

        try
        {
            user = applicationService.findUserByName(application, userKey);
        }
        catch (UserNotFoundException e)
        {
            user = null;
        }

        if (null == user)
        {
            // Justin Koke says that NONE password prevents logging in
            user = applicationService.addUser(application, new UserTemplate(userKey), PasswordCredential.NONE);
        }

        if (!applicationService.isUserDirectGroupMember(application, userKey, ATLASSIAN_CONNECT_ADD_ONS_USER_GROUP_KEY))
        {
            applicationService.addUserToGroup(application, userKey, ATLASSIAN_CONNECT_ADD_ONS_USER_GROUP_KEY);
        }

        // TODO ACDEV-933: enable user if disabled

        // TODO ACDEV-936: disable password recovery on this user

        return user;
    }
}
