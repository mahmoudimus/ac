package com.atlassian.jira.rest.client.p3.internal;

import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.json.SearchResultJsonParser;
import com.atlassian.jira.rest.client.p3.JiraSearchClient;
import com.atlassian.labs.remoteapps.api.service.http.HostHttpClient;
import com.atlassian.util.concurrent.Promise;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class P3JiraSearchClient extends AbstractP3RestClient implements JiraSearchClient
{
	private static final String START_AT_ATTRIBUTE = "startAt";
	private static final String MAX_RESULTS_ATTRIBUTE = "maxResults";
	private static final int MAX_JQL_LENGTH_FOR_HTTP_GET = 500;
	private static final String JQL_ATTRIBUTE = "jql";
	private final SearchResultJsonParser searchResultJsonParser = new SearchResultJsonParser();

	private static final String SEARCH_URI_PREFIX = "search";
	private final URI searchUri;

	public P3JiraSearchClient(HostHttpClient client) {
		super(client);
		searchUri = UriBuilder.fromUri(baseUri).path(SEARCH_URI_PREFIX).build();
	}

	@Override
	public Promise<SearchResult> searchJql(@Nullable String jql) {
		if (jql == null) {
			jql = "";
		}
		if (jql.length() > MAX_JQL_LENGTH_FOR_HTTP_GET) {
			final JSONObject postEntity = new JSONObject();
			try {
				postEntity.put(JQL_ATTRIBUTE, jql);
			} catch (JSONException e) {
				throw new RestClientException(e);
			}
            return callAndParse(client.newRequest(searchUri).setEntity(postEntity.toString()).post(), searchResultJsonParser);
		} else {
			final URI uri = UriBuilder.fromUri(searchUri).queryParam(JQL_ATTRIBUTE, jql).build();
			return callAndParse(client.newRequest(uri).get(), searchResultJsonParser);
		}
	}

	@Override
	public Promise<SearchResult> searchJql(@Nullable String jql, int maxResults, int startAt) {
		if (jql == null) {
			jql = "";
		}
		if (jql.length() > MAX_JQL_LENGTH_FOR_HTTP_GET) {
			final JSONObject postEntity = new JSONObject();
			try {
				postEntity.put(JQL_ATTRIBUTE, jql);
				postEntity.put(START_AT_ATTRIBUTE, startAt);
				postEntity.put(MAX_RESULTS_ATTRIBUTE, maxResults);
			} catch (JSONException e) {
				throw new RestClientException(e);
			}
            return callAndParse(client.newRequest(searchUri).setEntity(postEntity.toString()).post(), searchResultJsonParser);
		} else {
			final URI uri = UriBuilder.fromUri(searchUri).queryParam(JQL_ATTRIBUTE, jql).queryParam(MAX_RESULTS_ATTRIBUTE, maxResults)
					.queryParam(START_AT_ATTRIBUTE, startAt).build();
			return callAndParse(client.newRequest(uri).get(), searchResultJsonParser);
		}
	}
}
