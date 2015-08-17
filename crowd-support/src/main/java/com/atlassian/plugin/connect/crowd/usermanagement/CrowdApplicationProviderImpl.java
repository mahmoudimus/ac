package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.model.application.Application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CrowdApplicationProviderImpl implements CrowdApplicationProvider
{
    private final ApplicationManager applicationManager;

    @Autowired
    public CrowdApplicationProviderImpl(ApplicationManager applicationManager)
    {
        this.applicationManager = applicationManager;
    }

    @Override
    public Application getCrowdApplication() throws ApplicationNotFoundException
    {
        return applicationManager.findByName(getCrowdApplicationName());
    }

    @Override
    public String getCrowdApplicationName()
    {
        return "crowd-embedded";
    }
}
