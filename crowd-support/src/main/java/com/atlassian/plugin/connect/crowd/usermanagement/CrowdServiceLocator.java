package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;

public interface CrowdServiceLocator
{
    ConnectCrowdBase embedded(ApplicationService applicationService, UserReconciliation userReconciliation, ApplicationManager applicationManager);

    ConnectCrowdBase remote(CrowdClientProvider crowdClientProvider, UserReconciliation userReconciliation);
}
