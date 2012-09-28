package com.atlassian.jira.rest.client.p3;

import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.labs.remoteapps.api.Promise;

import javax.annotation.Nullable;

/**
 * The client handling search REST resource
 */
public interface JiraSearchClient
{
	/**
	 * Performs a JQL search and returns issues matching the query
	 *
	 * @param jql a valid JQL query (will be properly encoded by JIRA client). Restricted JQL characters (like '/') must be properly escaped.
	 * @return issues matching given JQL query
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid JQL query, etc.)
	 */
    Promise<SearchResult> searchJql(@Nullable String jql);

	/**
	 * Performs a JQL search and returns issues matching the query using default maxResults (as configured in JIRA - usually 50) and startAt=0
	 *
	 * @param jql a valid JQL query (will be properly encoded by JIRA client). Restricted JQL characters (like '/') must be properly escaped.
	 * @param maxResults maximum results (page/window size) for this search. The page will contain issues from
	 * <code>startAt div maxResults</code> (no remnant) and will include at most <code>maxResults</code> matching issues.
	 * @param startAt starting index (0-based) defining the page/window for the results. It will be aligned by the server to the beginning
	 * on the page (startAt = startAt div maxResults). For example for startAt=5 and maxResults=3 the results will include matching issues
	 * with index 3, 4 and 5. For startAt = 6 and maxResults=3 the issues returned are from position 6, 7 and 8.
	 * @return issues matching given JQL query
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid JQL query, etc.)
	 */
    Promise<SearchResult> searchJql(@Nullable String jql, int maxResults, int startAt);
}
