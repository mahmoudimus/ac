package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.plugin.connect.crowd.usermanagement.api.CrowdClientProvider;

public interface CrowdServiceFactory
{
    ConnectCrowdBase embedded(ApplicationService applicationService, UserReconciliation userReconciliation, ApplicationManager applicationManager);

    ConnectCrowdBase remote(CrowdClientProvider crowdClientProvider, UserReconciliation userReconciliation);
}
