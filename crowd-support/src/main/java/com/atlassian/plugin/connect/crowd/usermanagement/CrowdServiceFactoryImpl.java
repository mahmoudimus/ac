package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.plugin.connect.crowd.usermanagement.api.ConnectCrowdService;
import com.atlassian.plugin.connect.crowd.usermanagement.api.CrowdClientProvider;

import org.springframework.stereotype.Component;

@Component
public class CrowdServiceFactoryImpl implements CrowdServiceFactory
{
    @Override
    public ConnectCrowdService embedded(ApplicationService applicationService, UserReconciliation userReconciliation, ApplicationManager applicationManager)
    {
        return new EmbeddedCrowdService(applicationService, userReconciliation, applicationManager);
    }

    @Override
    public ConnectCrowdService remote(CrowdClientProvider crowdClientProvider, UserReconciliation userReconciliation)
    {
        return new RemoteCrowdService(crowdClientProvider, userReconciliation);
    }
}
