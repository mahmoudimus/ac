package com.atlassian.jira.rest.client.p3.internal;

import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.Project;
import com.atlassian.jira.rest.client.internal.json.BasicProjectsJsonParser;
import com.atlassian.jira.rest.client.internal.json.ProjectJsonParser;
import com.atlassian.jira.rest.client.p3.JiraProjectClient;
import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.api.service.http.HostHttpClient;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class P3JiraProjectClient extends AbstractP3RestClient implements JiraProjectClient
{
	private static final String PROJECT_URI_PREFIX = "project";
	private final ProjectJsonParser projectJsonParser = new ProjectJsonParser();
	private final BasicProjectsJsonParser basicProjectsJsonParser = new BasicProjectsJsonParser();

	public P3JiraProjectClient(HostHttpClient client) {
		super(client);
	}

	@Override
	public Promise<Project> getProject(final URI projectUri) {
        return callAndParse(client.newRequest(projectUri).get(), projectJsonParser);
	}

	@Override
	public Promise<Project> getProject(final String key) {
		final URI uri = UriBuilder.fromUri(baseUri).path(PROJECT_URI_PREFIX).path(key).build();
		return getProject(uri);
	}

	@Override
	public Promise<Iterable<BasicProject>> getAllProjects() {
		final URI uri = UriBuilder.fromUri(baseUri).path(PROJECT_URI_PREFIX).build();
        return callAndParse(client.newRequest(uri).get(), basicProjectsJsonParser);
	}
}
