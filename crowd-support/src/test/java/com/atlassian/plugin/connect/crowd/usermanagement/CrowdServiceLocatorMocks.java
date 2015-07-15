package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.plugin.connect.crowd.usermanagement.api.CrowdClientProvider;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class CrowdServiceLocatorMocks
{
    static void mockCrowdServiceLocator(CrowdServiceLocator crowdServiceLocator, ConnectCrowdBase embedded, ConnectCrowdBase remote)
    {
        when(crowdServiceLocator.embedded(
                any(ApplicationService.class), any(UserReconciliation.class),
                any(ApplicationManager.class))).thenReturn(embedded);

        when(crowdServiceLocator.remote(
                any(CrowdClientProvider.class), any(UserReconciliation.class))).thenReturn(remote);
    }
}
