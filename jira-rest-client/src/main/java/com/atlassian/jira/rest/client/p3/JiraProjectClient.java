package com.atlassian.jira.rest.client.p3;

import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.util.concurrent.Promise;

import java.net.URI;

/**
 * The client handling project resources.
 */
public interface JiraProjectClient
{
	/**
	 * Retrieves complete information about given project.
	 *
	 * @param key unique key of the project (usually 2+ characters)
	 * @return complete information about given project
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
    Promise<Project> getProject(String key);

	/**
	 * Retrieves complete information about given project.
	 * Use this method rather than {@link com.atlassian.jira.rest.client.p3.JiraProjectClient#getProject(String)}
	 * wheever you can, as this method is proof for potential changes of URI scheme used for exposing various
	 * resources by JIRA REST API.
	 *
	 * @param projectUri URI to project resource (usually get from <code>self</code> attribute describing component elsewhere
	 * @return complete information about given project
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
    Promise<Project> getProject(URI projectUri);

	/**
	 * Returns all projects, which are visible for the currently logged in user. If no user is logged in, it returns the
	 * list of projects that are visible when using anonymous access.
	 *
	 * @since client: 0.2, server 4.3
	 *
	 * @return projects which the currently logged user can see
	 */
    Promise<Iterable<BasicProject>> getAllProjects();

}
