package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Collections;
import java.util.Iterator;
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
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserGroupProvisioningService;
import com.atlassian.plugin.connect.api.usermanagment.ConnectAddOnUserInitException;
import com.atlassian.plugin.connect.spi.host.HostProperties;
import com.atlassian.plugin.connect.spi.product.FeatureManager;
import com.atlassian.plugin.connect.spi.user.ConnectAddOnUserDisableException;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This implementation seeks to encapsulate workarounds for:
 * <ul>
 *     <li>Crowd bugs like ACDEV-2037, which mean we need to use the crowd client in Confluence</li>
 *     <li>idiosyncrasies in Confluence and JIRA's use of Crowd Embedded (dealing directly with crowd embedded in Confluence leads to a race condition)</li>
 *     <li>Shortcomings in the implementation of Crowd user attributes (they don't sync)</li>
 *     <li>knowledge about how crowd works in OD vs. Server that we need to know because of the above</li>
 * </ul>
 * so that elsewhere, the business of adding users with attributes looks simple.
 */
@ExportAsDevService
@ConfluenceComponent
@JiraComponent
public class CloudAwareCrowdService implements ConnectCrowdService, ConnectAddOnUserGroupProvisioningService, ConnectCrowdSyncService
{
    private long syncTimeout = 10;
    private HostProperties hostProperties;
    private final FeatureManager featureManager;
    private final ConnectCrowdBase remote;
    private final ConnectCrowdBase embedded;
    private final ConcurrentHashMap<String, Map<String, Set<String>>> jiraPendingAttributes = new ConcurrentHashMap<>();
    private final TransferQueue<String> confluenceUsersToBeSynced = new LinkedTransferQueue<>();

    private static final Logger log = LoggerFactory.getLogger(CloudAwareCrowdService.class);

    @Autowired
    public CloudAwareCrowdService(CrowdServiceLocator crowdServiceLocator,
            ApplicationService applicationService, CrowdApplicationProvider crowdApplicationProvider,
            HostProperties hostProperties, FeatureManager featureManager,
            CrowdClientProvider crowdClientProvider, UserReconciliation userReconciliation)
    {
        this.hostProperties = hostProperties;
        this.featureManager = featureManager;
        this.remote = crowdServiceLocator.remote(crowdClientProvider, userReconciliation);
        this.embedded = crowdServiceLocator.embedded(applicationService, userReconciliation, crowdApplicationProvider);
    }

    @Override
    public UserCreationResult createOrEnableUser(String username, String displayName, String emailAddress, PasswordCredential passwordCredential)
    {
        return createOrEnableUser(username, displayName, emailAddress, passwordCredential, Collections.<String, Set<String>>emptyMap());
    }

    @Override
    public UserCreationResult createOrEnableUser(String username, String displayName, String emailAddress, PasswordCredential passwordCredential, Map<String, Set<String>> attributes)
    {
        UserCreationResult userCreationResult;
        if (isOnDemand())
        {
            if (isConfluence())
            {
                userCreationResult = createSyncedConfluenceUser(username, displayName, emailAddress, passwordCredential, attributes);
            }
            else
            {
                userCreationResult = embedded.createOrEnableUser(username, displayName, emailAddress, passwordCredential);
                if (!attributes.isEmpty())
                {
                    embedded.setAttributesOnUser(username, attributes);
                    jiraPendingAttributes.putIfAbsent(username, attributes);
                }
            }
        }
        else
        {
            userCreationResult = embedded.createOrEnableUser(username, displayName, emailAddress, passwordCredential);
            if (!attributes.isEmpty())
            {
                embedded.setAttributesOnUser(username, attributes);
            }
        }
        return userCreationResult;
    }

    private UserCreationResult createSyncedConfluenceUser(String username, String displayName, String emailAddress, PasswordCredential passwordCredential, Map<String, Set<String>> attributes)
    {
        UserCreationResult userCreationResult;
        try
        {
            userCreationResult = remote.createOrEnableUser(username, displayName, emailAddress, passwordCredential);
            if (!embedded.findUserByName(username).isPresent())
            {
                log.debug("queueing {} for sync", username);
                confluenceUsersToBeSynced.tryTransfer(username, syncTimeout, TimeUnit.SECONDS);

                // Double checking, in case the user synced after we checked but before we waited
                // ( or in case Crowd said it had finished the sync before it really had )
                if (!embedded.findUserByName(username).isPresent())
                {
                    throw new ConnectAddOnUserInitException("Could not find the user in the local Crowd cache");
                }
            }
            if (!attributes.isEmpty())
            {
                remote.setAttributesOnUser(username, attributes);
                embedded.setAttributesOnUser(username, attributes);
            }
        }
        catch (InterruptedException e)
        {
            throw new ConnectAddOnUserInitException(e);
        }
        return userCreationResult;
    }

    @Override
    public void handleSync()
    {
        if (isConfluence())
        {
            for (Iterator<String> iterator = confluenceUsersToBeSynced.iterator(); iterator.hasNext(); )
            {
                String toSync = iterator.next();
                if (embedded.findUserByName(toSync).isPresent())
                {
                    log.info("Acknowledging receipt of synced user {}", toSync);
                    iterator.remove();
                }
                else
                {
                    log.info("User {} hasn't synced yet - waiting for the next one, or a timeout", toSync);
                }
            }
        }
        else
        {
            // The user has been synchronised to the remote directory, so we can set the remote attribute
            for (Iterator<String> iterator = jiraPendingAttributes.keySet().iterator(); iterator.hasNext(); )
            {
                String toSync = iterator.next();
                if (embedded.findUserByName(toSync).isPresent())
                {
                    log.info("Setting attributes for {}", toSync);
                    Map<String, Set<String>> attributes = jiraPendingAttributes.get(toSync);
                    remote.setAttributesOnUser(toSync, attributes);
                    iterator.remove();
                }
                else
                {
                    log.info("User {} hasn't synced yet - awaiting the next sync, or a timeout", toSync);
                }
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
    public void ensureUserIsInGroups(String username, Set<String> groupNames)
            throws ApplicationNotFoundException, UserNotFoundException, ApplicationPermissionException, GroupNotFoundException, OperationFailedException, InvalidAuthenticationException
    {
        for(String groupName : groupNames)
        {
            ensureUserIsInGroup(username, groupName);
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
        else if (isJira() && isOnDemand())
        {
            boolean eCreated = embedded.ensureGroupExists(groupName);
            boolean rCreated = remote.ensureGroupExists(groupName);
            return eCreated || rCreated;
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
    @VisibleForTesting
    public boolean isUserActive(String username)
    {
        Optional<? extends User> userOption;
        if (isConfluence() && isOnDemand())
        {
            userOption = remote.findUserByName(username);
        }
        else
        {
            userOption = embedded.findUserByName(username);
        }
        return userOption.isPresent() && userOption.get().isActive();
    }

    @Override
    public void invalidateSessions(String username) throws OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException
    {
        if (isOnDemand())
        {
            remote.invalidateSessions(username);
        }
    }

    @VisibleForTesting
    void setSyncTimeout(long timeoutSeconds)
    {
        this.syncTimeout = timeoutSeconds;
    }

    private boolean isOnDemand()
    {
        return featureManager.isOnDemand();
    }

    private boolean isConfluence()
    {
        return hostProperties.getKey().equalsIgnoreCase("confluence");
    }

    private boolean isJira()
    {
        return hostProperties.getKey().equalsIgnoreCase("jira");
    }

}
