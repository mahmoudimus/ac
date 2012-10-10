package com.atlassian.jira.rest.client.p3;

import com.atlassian.jira.rest.client.domain.*;
import com.atlassian.util.concurrent.Promise;

import java.net.URI;

/**
 * Serves information about JIRA metadata like server information, issue types defined, stati, priorities and resolutions.
 * This data constitutes a data dictionary which then JIRA issues base on.
 */
public interface JiraMetadataClient
{
	/**
	 * Retrieves from the server complete information about selected issue type
	 * @param uri URI to issue type resource (one can get it e.g. from <code>self</code> attribute
	 * of issueType field of an issue).
	 * @return complete information about issue type resource
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
	Promise<IssueType> getIssueType(URI uri);

	/**
	 * Retrieves from the server complete list of available issue type
	 * @return complete information about issue type resource
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 * @since client 1.0, server 5.0
	 */
    Promise<Iterable<IssueType>> getIssueTypes();

    /**
     * Retrieves from the server complete list of available issue types
     * @return list of available issue types for this JIRA instance
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (if linking is disabled on the server, connectivity, malformed messages, etc.)
     * @since server 4.3, client 0.5
     */
    Promise<Iterable<IssuelinksType>> getIssueLinkTypes();

	/**
	 * Retrieves complete information about selected status
	 * @param uri URI to this status resource (one can get it e.g. from <code>self</code> attribute
	 * of <code>status</code> field of an issue)
	 * @return complete information about the selected status
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
    Promise<Status> getStatus(URI uri);

	/**
	 * Retrieves from the server complete information about selected priority
	 * @param uri URI for the priority resource
	 * @return complete information about the selected priority
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
    Promise<Priority> getPriority(URI uri);

	/**
	 * Retrieves from the server complete list of available priorities
	 * @return complete information about the selected priority
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 * @since client 1.0, server 5.0
	 */
    Promise<Iterable<Priority>> getPriorities();

	/**
	 * Retrieves from the server complete information about selected resolution
	 * @param uri URI for the resolution resource
	 * @return complete information about the selected resolution
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
    Promise<Resolution> getResolution(URI uri);

	/**
	 * Retrieves from the server complete information about selected resolution
	 * @return complete information about the selected resolution
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 * @since client 1.0, server 5.0
	 */
    Promise<Iterable<Resolution>> getResolutions();

	/**
	 * Retrieves information about this JIRA instance
	 * @return information about this JIRA instance
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
    Promise<ServerInfo> getServerInfo();
}
