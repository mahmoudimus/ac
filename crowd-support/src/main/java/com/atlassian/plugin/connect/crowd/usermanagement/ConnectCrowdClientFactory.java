package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.integration.rest.service.factory.RestCrowdClientFactory;
import com.atlassian.crowd.service.factory.CrowdClientFactory;

import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.springframework.stereotype.Component;

/**
 * A dummy subclass to allow access to the CrowdClientFactory, which JIRA and Confluence don't provide directly.
 *
 * Used to work around a crowd bug (https://ecosystem.atlassian.net/browse/EMBCWD-975)
 */
@SuppressWarnings ("UnusedDeclaration")
@JiraComponent
@ConfluenceComponent
public class ConnectCrowdClientFactory extends RestCrowdClientFactory implements CrowdClientFactory
{ }
