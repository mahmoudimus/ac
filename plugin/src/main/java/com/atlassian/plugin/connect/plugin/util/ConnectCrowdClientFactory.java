package com.atlassian.plugin.connect.plugin.util;

import com.atlassian.crowd.integration.rest.service.factory.RestCrowdClientFactory;
import com.atlassian.crowd.service.factory.CrowdClientFactory;

import org.springframework.stereotype.Component;

/**
 * A dummy subclass to allow access to the CrowdClientFactory, which JIRA and Confluence don't provide directly.
 *
 * Used by {@link com.atlassian.plugin.connect.plugin.usermanagement.ConnectAddOnUserServiceImpl and
 * {@link com.atlassian.plugin.connect.plugin.upgrade.ConnectAddOnUserAttributeUpgradeTask} to work around
 * a crowd bug (https://ecosystem.atlassian.net/browse/EMBCWD-975)
 */
@SuppressWarnings ("UnusedDeclaration")
@Component
public class ConnectCrowdClientFactory extends RestCrowdClientFactory implements CrowdClientFactory
{ }
