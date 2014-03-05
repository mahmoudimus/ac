package com.atlassian.plugin.connect.plugin.usermanagement;

import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

@ExportAsDevService
@Component
public class ConnectAddOnUserGroupProvisioningServiceImpl implements ConnectAddOnUserGroupProvisioningService
{
    private static final String CROWD_APPLICATION_NAME = "crowd-embedded"; // magic knowledge

    private final ApplicationService applicationService;
    private final ApplicationManager applicationManager;

    @Inject
    public ConnectAddOnUserGroupProvisioningServiceImpl(ApplicationService applicationService,
                                                        ApplicationManager applicationManager)
    {
        this.applicationService = checkNotNull(applicationService);
        this.applicationManager = checkNotNull(applicationManager);
    }

    @Override
    public void ensureUserIsInGroup(String userKey, String groupKey) throws ApplicationNotFoundException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException
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

    @Override
    public boolean ensureGroupExists(String groupKey) throws ApplicationNotFoundException, OperationFailedException, ApplicationPermissionException
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

    @Override
    public Group findGroupByKey(String groupKey) throws ApplicationNotFoundException
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

    @Override
    public String getCrowdApplicationName()
    {
        return CROWD_APPLICATION_NAME;
    }

    // Richard Atkins says that the Application is immutable and therefore the instance replaced every time changes occur,
    // and that therefore we should never cache it
    private Application getApplication() throws ApplicationNotFoundException
    {
        return applicationManager.findByName(CROWD_APPLICATION_NAME);
    }
}
