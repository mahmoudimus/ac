package com.atlassian.jira.rest.client.p3;

import com.atlassian.jira.rest.client.domain.User;
import com.atlassian.util.concurrent.Promise;

import java.net.URI;

/**
 * The client handling user resources.
 */
public interface JiraUserClient
{
	/**
	 * Retrieves detailed information about selected user.
	 * Try to use {@link #getUser(java.net.URI)} instead as that method is more RESTful (well connected)
	 *
	 * @param username JIRA username/login
	 * @return complete information about given user
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
    Promise<User> getUser(String username);

	/**
	 * Retrieves detailed information about selected user.
	 * This method is preferred over {@link #getUser(String)} as it's more RESTful (well connected)
	 *
	 * @param userUri URI of user resource
	 * @return complete information about given user
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
    Promise<User> getUser(URI userUri);
}
