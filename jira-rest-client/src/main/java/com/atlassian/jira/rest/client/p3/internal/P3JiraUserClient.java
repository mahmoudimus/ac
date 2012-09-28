package com.atlassian.jira.rest.client.p3.internal;

import com.atlassian.jira.rest.client.domain.User;
import com.atlassian.jira.rest.client.internal.json.UserJsonParser;
import com.atlassian.jira.rest.client.p3.JiraUserClient;
import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.api.service.http.HostHttpClient;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class P3JiraUserClient extends AbstractP3RestClient implements JiraUserClient
{
	private static final String USER_URI_PREFIX = "user";
	private final UserJsonParser userJsonParser = new UserJsonParser();

	public P3JiraUserClient(HostHttpClient client) {
		super(client);
	}

	@Override
	public Promise<User> getUser(String username) {
		final URI userUri = UriBuilder.fromUri(baseUri).path(USER_URI_PREFIX)
				.queryParam("username", username).queryParam("expand", "groups").build();
		return getUser(userUri);
	}

	@Override
	public Promise<User> getUser(URI userUri) {
        return callAndParse(client.newRequest(userUri).get(), userJsonParser);
	}
}
