package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.manager.application.ApplicationService;

/**
 * A utility intented to be used within the crowd-support package only,
 * for getting hold of services that talk to crowd in a specific way
 * (either via the client - {@link CrowdServiceLocator#embedded(ApplicationService, UserReconciliation, CrowdApplicationProvider)}
 * or local crowd services - {@link CrowdServiceLocator#remote(CrowdClientProvider, UserReconciliation)})
 */
public interface CrowdServiceLocator
{
    /**
     * @param applicationService A dependency of the embedded crowd service
     * @param userReconciliation A dependency of the embedded crowd service
     * @param crowdApplicationProvider A dependency of the embedded crowd service
     * @return A service for interacting with Crowd via embedded crowd services
     */
    ConnectCrowdBase embedded(ApplicationService applicationService, UserReconciliation userReconciliation, CrowdApplicationProvider crowdApplicationProvider);

    /**
     * @param crowdClientProvider A dependency of the remote crowd service
     * @param userReconciliation A dependency of the remote crowd service
     * @return A service for interacting with Crowd via remote crowd services
     */
    ConnectCrowdBase remote(CrowdClientProvider crowdClientProvider, UserReconciliation userReconciliation);
}
