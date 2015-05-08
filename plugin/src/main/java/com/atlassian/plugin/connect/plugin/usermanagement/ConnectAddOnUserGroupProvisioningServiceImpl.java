package com.atlassian.plugin.connect.plugin.usermanagement;

import javax.inject.Inject;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.MembershipAlreadyExistsException;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

@ExportAsDevService
@ConfluenceComponent
@JiraComponent
public class ConnectAddOnUserGroupProvisioningServiceImpl implements ConnectAddOnUserGroupProvisioningService
{
    private static final String CROWD_APPLICATION_NAME = "crowd-embedded"; // magic knowledge
    private static final Logger LOG = LoggerFactory.getLogger(ConnectAddOnUserGroupProvisioningServiceImpl.class);

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
        LOG.info("Attempting to make user '{}' a member of group '{}' (if not already a member).", userKey, groupKey);

        if (!applicationService.isUserDirectGroupMember(getCrowdApplication(), userKey, groupKey))
        {
            try
            {
                applicationService.addUserToGroup(getCrowdApplication(), userKey, groupKey);
                LOG.info("Added user '{}' to group '{}',", userKey, groupKey);
            }
            catch (MembershipAlreadyExistsException e)
            {
                // ignore, because the membership that we're trying to create exists
            }
        }
    }

    @Override
    public void removeUserFromGroup(String userKey, String groupKey) throws ApplicationNotFoundException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException
    {
        try
        {
            applicationService.removeUserFromGroup(getCrowdApplication(), userKey, groupKey);
            LOG.info("Removed user '{}' from group '{}'.", userKey, groupKey);
        }
        catch (MembershipNotFoundException e)
        {
            // ignore, we wanted to remove the member anyway
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
                applicationService.addGroup(getCrowdApplication(), new GroupTemplate(groupKey));
                created = true;
                LOG.info("Created group '{}'.", groupKey);
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
            group = applicationService.findGroupByName(getCrowdApplication(), groupKey);
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

    @Override
    public Application getCrowdApplication() throws ApplicationNotFoundException
    {
        return applicationManager.findByName(CROWD_APPLICATION_NAME);
    }
}
