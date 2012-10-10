package com.atlassian.jira.rest.client.p3;

import com.atlassian.jira.rest.client.domain.Component;
import com.atlassian.jira.rest.client.domain.input.ComponentInput;
import com.atlassian.util.concurrent.Promise;

import javax.annotation.Nullable;
import java.net.URI;

public interface JiraComponentClient
{
	/**
	 * @param componentUri URI to selected component resource
	 * @return complete information about selected component
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
	Promise<Component> getComponent(URI componentUri);

    Promise<Component> createComponent(String projectKey, ComponentInput componentInput);

    Promise<Component> updateComponent(URI componentUri, ComponentInput componentInput);

    Promise<Void> removeComponent(URI componentUri, @Nullable URI moveIssueToComponentUri);

    Promise<Integer> getComponentRelatedIssuesCount(URI componentUri);
}
