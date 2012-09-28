package com.atlassian.jira.rest.client.p3.internal;

import com.atlassian.jira.rest.client.domain.Component;
import com.atlassian.jira.rest.client.domain.input.ComponentInput;
import com.atlassian.jira.rest.client.internal.domain.input.ComponentInputWithProjectKey;
import com.atlassian.jira.rest.client.internal.json.ComponentJsonParser;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import com.atlassian.jira.rest.client.internal.json.gen.ComponentInputWithProjectKeyJsonGenerator;
import com.atlassian.jira.rest.client.p3.JiraComponentClient;
import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.api.service.http.HostHttpClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class P3JiraComponentClient extends AbstractP3RestClient implements JiraComponentClient
{
	private final ComponentJsonParser componentJsonParser = new ComponentJsonParser();
	private final URI componentUri;

	public P3JiraComponentClient(HostHttpClient client) {
		super(client);
		componentUri = UriBuilder.fromUri(baseUri).path("component").build();
	}

	@Override
	public Promise<Component> getComponent(final URI componentUri) {
		return callAndParse(client.newRequest(componentUri).get(), componentJsonParser);
	}

	@Override
	public Promise<Component> createComponent(String projectKey, ComponentInput componentInput) {
		final ComponentInputWithProjectKey helper = new ComponentInputWithProjectKey(projectKey, componentInput);
		return callAndParse(
                client.newRequest(componentUri)
                      .setEntity(toEntity(new ComponentInputWithProjectKeyJsonGenerator(), helper)).post(),
                new ComponentJsonParser());
	}

	@Override
	public Promise<Component> updateComponent(URI componentUri, ComponentInput componentInput) {
		final ComponentInputWithProjectKey helper = new ComponentInputWithProjectKey(null, componentInput);
        return callAndParse(client.newRequest(componentUri)
                                  .setEntity(toEntity(new ComponentInputWithProjectKeyJsonGenerator(), helper))
                                  .put(), new ComponentJsonParser());
	}

	@Override
	public Promise<Void> removeComponent(URI componentUri, @Nullable URI moveIssueToComponentUri) {
		final UriBuilder uriBuilder = UriBuilder.fromUri(componentUri);
		if (moveIssueToComponentUri != null) {
			uriBuilder.queryParam("moveIssuesTo", moveIssueToComponentUri);
		}
        return call(client.newRequest(componentUri).delete());
	}

	@Override
	public Promise<Integer> getComponentRelatedIssuesCount(URI componentUri) {
		final URI relatedIssueCountsUri = UriBuilder.fromUri(componentUri).path("relatedIssueCounts").build();
        return callAndParse(client.newRequest(relatedIssueCountsUri).get(), new JsonObjectParser<Integer>()
        {
            @Override
            public Integer parse(JSONObject json) throws JSONException
            {
                return json.getInt("issueCount");
            }
        });
	}
}
