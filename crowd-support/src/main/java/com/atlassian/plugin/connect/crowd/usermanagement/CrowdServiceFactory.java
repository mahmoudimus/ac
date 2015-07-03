package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.plugin.connect.crowd.usermanagement.api.ConnectCrowdService;
import com.atlassian.plugin.connect.crowd.usermanagement.api.CrowdClientProvider;

public interface CrowdServiceFactory
{
    public ConnectCrowdService embedded(ApplicationService applicationService, UserReconciliation userReconciliation, ApplicationManager applicationManager);

    public ConnectCrowdService remote(CrowdClientProvider crowdClientProvider, UserReconciliation userReconciliation);
}
