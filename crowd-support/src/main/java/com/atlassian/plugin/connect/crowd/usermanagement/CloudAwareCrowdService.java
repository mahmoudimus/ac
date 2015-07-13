package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.crowd.usermanagement.api.ConnectCrowdService;
import com.atlassian.plugin.connect.spi.host.HostProperties;
import com.atlassian.plugin.connect.spi.product.FeatureManager;
import com.atlassian.plugin.connect.spi.user.ConnectAddOnUserDisableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This implementation seeks to encapsulate workarounds for:
 * <ul>
 *     <li>Crowd bugs like ACDEV-2037, which mean we need to use the crowd client in Confluence</li>
 *     <li>idiosyncrasies in Confluence and JIRA's use of Crowd Embedded</li>
 *     <li>Shortcomings in the implementation of Crowd user attributes</li>
 *     <li>knowledge about how crowd works in OD vs. Server that we need to know because of the above</li>
 * </ul>
 * so that elsewhere, the business of adding users and attributes looks simple.
 */
@Component
public class CloudAwareCrowdService implements ConnectCrowdService, ConnectAddOnUserGroupProvisioningService, ConnectCrowdSyncService
{
    private HostProperties hostProperties;
    private final FeatureManager featureManager;
    private final ConnectCrowdBase remote;
    private final ConnectCrowdBase embedded;
    private final ConcurrentHashMap<String, Map<String, Set<String>>> jiraPendingAttributes = new ConcurrentHashMap<>();
    private final TransferQueue<String> confluenceUsersToBeSynced = new LinkedTransferQueue<>();

    private static final Logger log = LoggerFactory.getLogger(CloudAwareCrowdService.class);

    @Autowired
    public CloudAwareCrowdService(CrowdServiceFactory crowdServiceLocator,
            ApplicationService applicationService, ApplicationManager applicationManager,
            HostProperties hostProperties, FeatureManager featureManager,
            CrowdClientProviderImpl crowdClientProvider, UserReconciliation userReconciliation)
    {
        this.hostProperties = hostProperties;
        this.featureManager = featureManager;
        this.remote = crowdServiceLocator.remote(crowdClientProvider, userReconciliation);
        this.embedded = crowdServiceLocator.embedded(applicationService, userReconciliation, applicationManager);
    }

    @Override
    public User createOrEnableUser(String username, String displayName, String emailAddress, PasswordCredential passwordCredential)
    {
        return createOrEnableUser(username, displayName, emailAddress, passwordCredential, Collections.<String, Set<String>>emptyMap());
    }

    @Override
    public User createOrEnableUser(String username, String displayName, String emailAddress, PasswordCredential passwordCredential, Map<String, Set<String>> attributes)
    {
        User user;
        if (isOnDemand())
        {
            if (isConfluence())
            {
                user = createSyncedConfluenceUser(username, displayName, emailAddress, passwordCredential, attributes);
            }
            else
            {
                user = embedded.createOrEnableUser(username, displayName, emailAddress, passwordCredential);
                embedded.setAttributesOnUser(username, attributes);
                jiraPendingAttributes.putIfAbsent(username, attributes);
            }
        }
        else
        {
            user = embedded.createOrEnableUser(username, displayName, emailAddress, passwordCredential);
            embedded.setAttributesOnUser(username, attributes);
        }
        return user;
    }

    private User createSyncedConfluenceUser(String username, String displayName, String emailAddress, PasswordCredential passwordCredential, Map<String, Set<String>> attributes)
    {
        User user;
        try
        {
            if (!embedded.findUserByName(username).isPresent())
            {
                user = remote.createOrEnableUser(username, displayName, emailAddress, passwordCredential);
                log.debug("queueing {} for sync", username);
                boolean synced = confluenceUsersToBeSynced.tryTransfer(username, 10, TimeUnit.SECONDS);
                // Double checking
                if (!synced && !embedded.findUserByName(username).isPresent())
                {
                    throw new ConnectAddOnUserInitException("Could not find the user in the local Crowd cache");
                }
            }
            else
            {
                user = remote.createOrEnableUser(username, displayName, emailAddress, passwordCredential);
            }
            remote.setAttributesOnUser(username, attributes);
            embedded.setAttributesOnUser(username, attributes);
        }
        catch (InterruptedException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
        return user;
    }

    @Override
    public void handleSync(String username)
    {
        // The sync has completed so the remote directory should now have a copy
        // of all the users we want to set an attribute on.
        if (isConfluence())
        {
            boolean wasQueued = confluenceUsersToBeSynced.remove(username);
            if (wasQueued) {
                log.debug("Acknowledged synced user {}", username);
            }
        }
        else
        {
            Map<String, Set<String>> attributes = jiraPendingAttributes.remove(username);
            if (attributes != null)
            {
                log.debug("Set attributes for {}", username);
                remote.setAttributesOnUser(username, attributes);
            }
        }
    }

    @Override
    public void disableUser(String username)
            throws ConnectAddOnUserDisableException
    {
        if (isConfluence() && isOnDemand())
        {
            remote.disableUser(username);
        }
        else
        {
            embedded.disableUser(username);
        }
    }

    @Override
    public void ensureUserIsInGroup(String username, String groupName)
            throws ApplicationNotFoundException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException, InvalidAuthenticationException
    {
        if (isConfluence() && isOnDemand())
        {
            remote.ensureUserIsInGroup(username, groupName);
        }
        else
        {
            embedded.ensureUserIsInGroup(username, groupName);
        }
    }

    @Override
    public void removeUserFromGroup(String username, String groupName)
            throws ApplicationNotFoundException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException, InvalidAuthenticationException
    {
        if (isConfluence() && isOnDemand())
        {
            remote.removeUserFromGroup(username, groupName);
        }
        else
        {
            embedded.removeUserFromGroup(username, groupName);
        }
    }

    @Override
    public boolean ensureGroupExists(String groupName)
            throws ApplicationNotFoundException, OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException
    {
        if (isConfluence() && isOnDemand())
        {
            return remote.ensureGroupExists(groupName);
        }
        else
        {
            return embedded.ensureGroupExists(groupName);
        }
    }

    @Override
    public Group findGroupByKey(String groupName)
            throws ApplicationNotFoundException, ApplicationPermissionException, InvalidAuthenticationException
    {
        if (isConfluence() && isOnDemand())
        {
            return remote.findGroupByKey(groupName);
        }
        else
        {
            return embedded.findGroupByKey(groupName);
        }
    }

    @Override
    public String getCrowdApplicationName()
    {
        return embedded.getCrowdApplicationName();
    }

    @Override
    public Application getCrowdApplication() throws ApplicationNotFoundException
    {
        return embedded.getCrowdApplication();
    }

    private boolean isOnDemand()
    {
        return featureManager.isOnDemand();
    }

    private boolean isConfluence()
    {
        return hostProperties.getKey().equalsIgnoreCase("confluence");
    }
}
