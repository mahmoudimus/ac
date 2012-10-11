package com.atlassian.jira.rest.client.p3.internal;

import com.atlassian.jira.rest.client.domain.*;
import com.atlassian.jira.rest.client.internal.json.*;
import com.atlassian.jira.rest.client.p3.JiraMetadataClient;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.util.concurrent.Promise;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class P3JiraMetadataClient extends AbstractP3RestClient implements JiraMetadataClient
{
	private final String SERVER_INFO_RESOURCE = "/serverInfo";
	private final ServerInfoJsonParser serverInfoJsonParser = new ServerInfoJsonParser();
	private final IssueTypeJsonParser issueTypeJsonParser = new IssueTypeJsonParser();
	private final GenericJsonArrayParser<IssueType> issueTypesJsonParser = GenericJsonArrayParser.create(issueTypeJsonParser);
	private final StatusJsonParser statusJsonParser = new StatusJsonParser();
	private final PriorityJsonParser priorityJsonParser = new PriorityJsonParser();
	private final GenericJsonArrayParser<Priority> prioritiesJsonParser = GenericJsonArrayParser.create(priorityJsonParser);
	private final ResolutionJsonParser resolutionJsonParser = new ResolutionJsonParser();
	private final GenericJsonArrayParser<Resolution> resolutionsJsonParser = GenericJsonArrayParser.create(resolutionJsonParser);
	private final IssueLinkTypesJsonParser issueLinkTypesJsonParser = new IssueLinkTypesJsonParser();

	public P3JiraMetadataClient(HostHttpClient client) {
		super(client);
	}

	@Override
	public Promise<IssueType> getIssueType(final URI uri) {
		return callAndParse(client.newRequest(uri).get(), issueTypeJsonParser);
	}

	@Override
	public Promise<Iterable<IssueType>> getIssueTypes() {
		final URI uri = UriBuilder.fromUri(baseUri).path("issuetype").build();
        return callAndParse(client.newRequest(uri).get(), issueTypesJsonParser);
	}

	@Override
	public Promise<Iterable<IssuelinksType>> getIssueLinkTypes() {
		final URI uri = UriBuilder.fromUri(baseUri).path("issueLinkType").build();
        return callAndParse(client.newRequest(uri).get(), issueLinkTypesJsonParser);
	}

	@Override
	public Promise<Status> getStatus(final URI uri) {
        return callAndParse(client.newRequest(uri).get(), statusJsonParser);
	}

	@Override
	public Promise<Priority> getPriority(final URI uri) {
        return callAndParse(client.newRequest(uri).get(), priorityJsonParser);
	}

	@Override
	public Promise<Iterable<Priority>> getPriorities() {
		final URI uri = UriBuilder.fromUri(baseUri).path("priority").build();
        return callAndParse(client.newRequest(uri).get(), prioritiesJsonParser);
	}

	@Override
	public Promise<Resolution> getResolution(URI uri) {
        return callAndParse(client.newRequest(uri).get(), resolutionJsonParser);
	}

	@Override
	public Promise<Iterable<Resolution>> getResolutions() {
		final URI uri = UriBuilder.fromUri(baseUri).path("resolution").build();
        return callAndParse(client.newRequest(uri).get(), resolutionsJsonParser);
	}

	@Override
	public Promise<ServerInfo> getServerInfo() {
        return callAndParse(client.newRequest(UriBuilder.fromUri(baseUri)
                .path(SERVER_INFO_RESOURCE).build()).get(), serverInfoJsonParser);
	}
}
