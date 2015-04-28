package com.atlassian.plugin.connect.plugin.usermanagement;

import com.atlassian.crowd.service.factory.CrowdClientFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class JiraCrowdClientFacade extends CrowdClientFacadeImplBase implements CrowdClientFacade
{
    @Autowired
    public JiraCrowdClientFacade(CrowdClientFactory crowdClientFactory)
    {
        super(crowdClientFactory);
    }

    @Override
    public String getClientApplicationName()
    {
        return "jira";
    }
}
