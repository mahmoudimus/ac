package com.atlassian.plugin.remotable.host.common.service.jira;

import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.VersionRelatedIssuesCount;
import com.atlassian.jira.rest.client.domain.input.VersionInput;
import com.atlassian.jira.rest.client.domain.input.VersionPosition;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import com.atlassian.jira.rest.client.internal.json.VersionJsonParser;
import com.atlassian.jira.rest.client.internal.json.VersionRelatedIssueCountJsonParser;
import com.atlassian.jira.rest.client.internal.json.gen.JsonGenerator;
import com.atlassian.jira.rest.client.internal.json.gen.VersionInputJsonGenerator;
import com.atlassian.jira.rest.client.internal.json.gen.VersionPositionInputGenerator;
import com.atlassian.plugin.remotable.api.service.jira.JiraVersionClient;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.util.concurrent.Promise;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class P3JiraVersionClient extends AbstractP3RestClient implements JiraVersionClient
{
	private final URI versionRootUri;

	public P3JiraVersionClient(HostHttpClient client) {
		super(client);
		versionRootUri = UriBuilder.fromUri(baseUri).path("version").build();
	}

	@Override
	public Promise<Version> createVersion(final VersionInput version) {
        return callAndParse(client.newRequest(versionRootUri).setEntity(
                toEntity(new VersionInputJsonGenerator(), version)).post(), new VersionJsonParser());
	}

	@Override
	public Promise<Version> updateVersion(URI versionUri, final VersionInput version) {
		return callAndParse(client.newRequest(versionUri).setEntity(
                toEntity(new VersionInputJsonGenerator(), version)).put(), new VersionJsonParser());
	}

	@Override
	public Promise<Void> removeVersion(URI versionUri, @Nullable URI moveFixIssuesToVersionUri,
			@Nullable URI moveAffectedIssuesToVersionUri) {
		final UriBuilder uriBuilder = UriBuilder.fromUri(versionUri);
		if (moveFixIssuesToVersionUri != null) {
			uriBuilder.queryParam("moveFixIssuesTo", moveFixIssuesToVersionUri);
		}
		if (moveAffectedIssuesToVersionUri != null) {
			uriBuilder.queryParam("moveAffectedIssuesTo", moveAffectedIssuesToVersionUri);
		}
        return call(client.newRequest(uriBuilder.build()).delete());
	}

	@Override
	public Promise<Version> getVersion(URI versionUri) {
        return callAndParse(client.newRequest(versionUri).get(), new VersionJsonParser());
	}

	@Override
	public Promise<VersionRelatedIssuesCount> getVersionRelatedIssuesCount(URI versionUri) {
		final URI relatedIssueCountsUri = UriBuilder.fromUri(versionUri).path("relatedIssueCounts").build();
        return callAndParse(client.newRequest(relatedIssueCountsUri).get(), new VersionRelatedIssueCountJsonParser());
	}

	@Override
	public Promise<Integer> getNumUnresolvedIssues(URI versionUri) {
		final URI unresolvedIssueCountUri = UriBuilder.fromUri(versionUri).path("unresolvedIssueCount").build();
        return callAndParse(client.newRequest(unresolvedIssueCountUri).get(), new JsonObjectParser<Integer>() {
			@Override
			public Integer parse(JSONObject json) throws JSONException {
				return json.getInt("issuesUnresolvedCount");
			}
		});
	}

	@Override
	public Promise<Version> moveVersionAfter(URI versionUri, URI afterVersionUri) {
		final URI moveUri = getMoveVersionUri(versionUri);

        return callAndParse(client.newRequest(moveUri).setEntity(toEntity(new JsonGenerator<URI>() {
			@Override
			public JSONObject generate(URI uri) throws JSONException {
				final JSONObject res = new JSONObject();
				res.put("after", uri);
				return res;
			}
		}, afterVersionUri)).post(), new VersionJsonParser());
	}

	@Override
	public Promise<Version> moveVersion(URI versionUri, final VersionPosition versionPosition) {
		final URI moveUri = getMoveVersionUri(versionUri);
        return callAndParse(client.newRequest(moveUri).setEntity(toEntity(new VersionPositionInputGenerator(), versionPosition)).post(),
				new VersionJsonParser());
	}

	private URI getMoveVersionUri(URI versionUri) {
		return UriBuilder.fromUri(versionUri).path("move").build();
	}
}
