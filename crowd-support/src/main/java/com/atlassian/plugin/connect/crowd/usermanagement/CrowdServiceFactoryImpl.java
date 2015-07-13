package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.plugin.connect.crowd.usermanagement.api.CrowdClientProvider;

import org.springframework.stereotype.Component;

@Component
public class CrowdServiceFactoryImpl implements CrowdServiceFactory
{
    @Override
    public ConnectCrowdBase embedded(ApplicationService applicationService, UserReconciliation userReconciliation, ApplicationManager applicationManager)
    {
        return new EmbeddedCrowd(applicationService, userReconciliation, applicationManager);
    }

    @Override
    public ConnectCrowdBase remote(CrowdClientProvider crowdClientProvider, UserReconciliation userReconciliation)
    {
        return new RemoteCrowd(crowdClientProvider, userReconciliation);
    }
}
