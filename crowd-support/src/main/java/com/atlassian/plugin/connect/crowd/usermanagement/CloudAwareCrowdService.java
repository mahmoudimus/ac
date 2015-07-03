package com.atlassian.plugin.connect.crowd.usermanagement;

import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.plugin.connect.crowd.usermanagement.api.ConnectCrowdException;
import com.atlassian.plugin.connect.crowd.usermanagement.api.ConnectCrowdService;
import com.atlassian.plugin.connect.spi.host.HostProperties;
import com.atlassian.plugin.connect.spi.product.FeatureManager;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

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
@JiraComponent
@ConfluenceComponent
public class CloudAwareCrowdService implements ConnectCrowdService
{
    private HostProperties hostProperties;
    private final FeatureManager featureManager;
    private final ConnectCrowdService remote;
    private final ConnectCrowdService embedded;

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
            throws ConnectCrowdException
    {
        if (isConfluence() && isOnDemand())
        {
            return remote.createOrEnableUser(username, displayName, emailAddress, passwordCredential);
        }
        else
        {
            return embedded.createOrEnableUser(username, displayName, emailAddress, passwordCredential);
        }
    }

    @Override
    public void disableUser(String username) throws ConnectCrowdException
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
    public void setAttributesOnUser(User user, Map<String, Set<String>> attributes)
            throws ConnectCrowdException
    {
        embedded.setAttributesOnUser(user, attributes);

        if (isOnDemand())
        {
            // Sets the connect attribute on the Remote Crowd Server if running in OD
            // This is currently required due to the fact that the DbCachingRemoteDirectory implementation used by JIRA and Confluence doesn't currently
            // write attributes back to the Crowd Server. This can be removed completely with Crowd 2.9 since addUser can take a UserWithAttributes in this version
            remote.setAttributesOnUser(user, attributes);
        }
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
