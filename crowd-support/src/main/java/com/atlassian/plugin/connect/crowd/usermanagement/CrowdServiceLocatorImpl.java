package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.manager.application.ApplicationService;

import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.stereotype.Component;

@ConfluenceComponent
@JiraComponent
public class CrowdServiceLocatorImpl implements CrowdServiceLocator
{
    @Override
    public ConnectCrowdBase embedded(ApplicationService applicationService, UserReconciliation userReconciliation, CrowdApplicationProvider crowdApplicationProvider)
    {
        return new EmbeddedCrowd(applicationService, userReconciliation, crowdApplicationProvider);
    }

    @Override
    public ConnectCrowdBase remote(CrowdClientProvider crowdClientProvider, UserReconciliation userReconciliation)
    {
        return new RemoteCrowd(crowdClientProvider, userReconciliation);
    }
}
