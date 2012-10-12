package com.atlassian.plugin.remotable.host.common.service.jira;

import com.atlassian.httpclient.api.Request;
import com.atlassian.httpclient.api.Response;
import com.atlassian.jira.rest.client.GetCreateIssueMetadataOptions;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.CimProject;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.ServerInfo;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.Votes;
import com.atlassian.jira.rest.client.domain.Watchers;
import com.atlassian.jira.rest.client.domain.input.AttachmentInput;
import com.atlassian.jira.rest.client.domain.input.FieldInput;
import com.atlassian.jira.rest.client.domain.input.IssueInput;
import com.atlassian.jira.rest.client.domain.input.LinkIssuesInput;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.domain.input.WorklogInput;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;
import com.atlassian.jira.rest.client.internal.json.BasicIssueJsonParser;
import com.atlassian.jira.rest.client.internal.json.CreateIssueMetadataJsonParser;
import com.atlassian.jira.rest.client.internal.json.IssueJsonParser;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import com.atlassian.jira.rest.client.internal.json.TransitionJsonParser;
import com.atlassian.jira.rest.client.internal.json.TransitionJsonParserV5;
import com.atlassian.jira.rest.client.internal.json.VotesJsonParser;
import com.atlassian.jira.rest.client.internal.json.WatchersJsonParserBuilder;
import com.atlassian.jira.rest.client.internal.json.gen.CommentJsonGenerator;
import com.atlassian.jira.rest.client.internal.json.gen.IssueInputJsonGenerator;
import com.atlassian.jira.rest.client.internal.json.gen.LinkIssuesInputGenerator;
import com.atlassian.jira.rest.client.internal.json.gen.WorklogInputJsonGenerator;
import com.atlassian.plugin.remotable.api.service.jira.JiraIssueClient;
import com.atlassian.plugin.remotable.api.service.jira.JiraMetadataClient;
import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.plugin.remotable.api.service.http.HostHttpClient;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;

public class P3JiraIssueClient extends AbstractP3RestClient implements JiraIssueClient
{
	private static final EnumSet<Expandos> DEFAULT_EXPANDS = EnumSet.of(Expandos.NAMES, Expandos.SCHEMA, Expandos.TRANSITIONS);
	private static final Function<Expandos, String> EXPANDO_TO_PARAM = new Function<Expandos, String>() {
		@Override
		public String apply(Expandos from) {
			return from.name().toLowerCase();
		}
	};
	private final RequestContext requestContext;
	private final JiraMetadataClient metadataRestClient;

	private final IssueJsonParser issueParser = new IssueJsonParser();
	private final BasicIssueJsonParser basicIssueParser = new BasicIssueJsonParser();
	private final JsonObjectParser<Watchers> watchersParser = WatchersJsonParserBuilder.createWatchersParser();
	private final TransitionJsonParser transitionJsonParser = new TransitionJsonParser();
	private final JsonObjectParser<Transition> transitionJsonParserV5 = new TransitionJsonParserV5();
	private final VotesJsonParser votesJsonParser = new VotesJsonParser();
	private final CreateIssueMetadataJsonParser createIssueMetadataJsonParser = new CreateIssueMetadataJsonParser();
	private ServerInfo serverInfo;

	public P3JiraIssueClient(HostHttpClient client,
            RequestContext requestContext, JiraMetadataClient metadataRestClient) {
		super(client);
        this.requestContext = requestContext;
		this.metadataRestClient = metadataRestClient;
	}

	private synchronized ServerInfo getVersionInfo() {
		if (serverInfo == null) {
			serverInfo = metadataRestClient.getServerInfo().claim();
		}
		return serverInfo;
	}

	@Override
	public Promise<Watchers> getWatchers(URI watchersUri) {
		return callAndParse(client.newRequest(watchersUri).get(), watchersParser);
	}


	@Override
	public Promise<Votes> getVotes(URI votesUri) {
		return callAndParse(client.newRequest(votesUri).get(), votesJsonParser);
	}

	@Override
	public Promise<Issue> getIssue(final String issueKey) {
		return getIssue(issueKey, Collections.<Expandos>emptyList());
	}

	@Override
	public Promise<Issue> getIssue(final String issueKey, Iterable<Expandos> expand) {
		final UriBuilder uriBuilder = UriBuilder.fromUri(baseUri);
		final Iterable<Expandos> expands = Iterables.concat(DEFAULT_EXPANDS, expand);
		uriBuilder.path("issue").path(issueKey).queryParam("expand",
				Joiner.on(',').join(Iterables.transform(expands, EXPANDO_TO_PARAM)));
		return callAndParse(client.newRequest(uriBuilder.build()).get(), issueParser);
	}

	@Override
	public Promise<Iterable<Transition>> getTransitions(final URI transitionsUri) {
        return callAndParse(client.newRequest(transitionsUri).get(), new ResponseHandler<Iterable<Transition>>()
        {
            @Override
            public Iterable<Transition> handle(Response response) throws JSONException, IOException
            {
                JSONObject jsonObject = new JSONObject(response.getEntity());
                if (jsonObject.has("transitions")) {
                    return JsonParseUtil.parseJsonArray(jsonObject.getJSONArray("transitions"),
                                    transitionJsonParserV5);
                } else {
                    final Collection<Transition> transitions = new ArrayList<Transition>(jsonObject.length());
                    @SuppressWarnings("unchecked")
                    final Iterator<String> iterator = jsonObject.keys();
                    while (iterator.hasNext()) {
                        final String key = iterator.next();
                        try {
                            final int id = Integer.parseInt(key);
                            final Transition transition = transitionJsonParser.parse(jsonObject.getJSONObject(key), id);
                            transitions.add(transition);
                        } catch (JSONException e) {
                            throw new RestClientException(e);
                        } catch (NumberFormatException e) {
                            throw new RestClientException("Transition id should be an integer, but found [" + key + "]", e);
                        }
                    }
                    return transitions;
                }
            }
        });
	}

	@Override
	public Promise<Iterable<Transition>> getTransitions(final Issue issue) {
		if (issue.getTransitionsUri() != null) {
			return getTransitions(issue.getTransitionsUri());
		} else {
			final UriBuilder transitionsUri = UriBuilder.fromUri(issue.getSelf());
			return getTransitions(transitionsUri.path("transitions")
					.queryParam("expand", "transitions.fields").build());
		}
	}

	@Override
	public Promise<Void> transition(final URI transitionsUri, final TransitionInput transitionInput) {
		final int buildNumber = getVersionInfo().getBuildNumber();
        try
        {
            JSONObject jsonObject = new JSONObject();
            if (buildNumber >= ServerVersionConstants.BN_JIRA_5) {
                jsonObject.put("transition", new JSONObject().put("id", transitionInput.getId()));
            } else {
                jsonObject.put("transition", transitionInput.getId());
            }
            if (transitionInput.getComment() != null) {
                if (buildNumber >= ServerVersionConstants.BN_JIRA_5) {
                    jsonObject.put("update", new JSONObject().put("comment",
                            new JSONArray().put(new JSONObject().put("add",
                                    new CommentJsonGenerator(getVersionInfo())
                                            .generate(transitionInput.getComment())))));
                } else {
                    jsonObject.put("comment", new CommentJsonGenerator(getVersionInfo())
                            .generate(transitionInput.getComment()));
                }
            }
            JSONObject fieldsJs = new JSONObject();
            final Iterable<FieldInput> fields = transitionInput.getFields();
            if (fields.iterator().hasNext()) {
                for (FieldInput fieldInput : fields) {
                    fieldsJs.put(fieldInput.getId(), fieldInput.getValue());
                }
            }
            if (fieldsJs.keys().hasNext()) {
                jsonObject.put("fields", fieldsJs);
            }
            return call(client.newRequest(transitionsUri).setEntity(jsonObject.toString()).post());
        }
        catch (JSONException ex)
        {
            throw new RestClientException(ex);
        }
	}

	@Override
	public Promise<Void> transition(final Issue issue, final TransitionInput transitionInput) {
		if (issue.getTransitionsUri() != null) {
			return transition(issue.getTransitionsUri(), transitionInput);
		} else {
			final UriBuilder uriBuilder = UriBuilder.fromUri(issue.getSelf());
			uriBuilder.path("transitions");
			return transition(uriBuilder.build(), transitionInput);
		}
	}


	@Override
	public Promise<Void> vote(final URI votesUri) {
        return call(client.newRequest(votesUri).post());
	}

	@Override
	public Promise<Void> unvote(final URI votesUri) {
        return call(client.newRequest(votesUri).delete());
	}

	@Override
	public Promise<Void> addWatcher(final URI watchersUri, @Nullable final String username) {
        Request request = client.newRequest(watchersUri);
        if (username != null)
        {
            request.setEntity(JSONObject.quote(username));
        }
        return call(request.post());
	}

	@Override
	public Promise<Void> removeWatcher(final URI watchersUri, final String username) {
		final UriBuilder uriBuilder = UriBuilder.fromUri(watchersUri);
		if (getVersionInfo().getBuildNumber() >= ServerVersionConstants.BN_JIRA_4_4) {
			uriBuilder.queryParam("username", username);
		} else {
			uriBuilder.path(username).build();
		}
        return call(client.newRequest(uriBuilder.build()).delete());
	}

	@Override
	public Promise<Void> linkIssue(final LinkIssuesInput linkIssuesInput) {
		final URI uri = UriBuilder.fromUri(baseUri).path("issueLink").build();
        return call(client.newRequest(uri).setEntity(toEntity(new LinkIssuesInputGenerator(getVersionInfo()), linkIssuesInput)).post());
	}

	@Override
	public Promise<Void> addAttachment(final URI attachmentsUri, final InputStream in, final String filename) {
		return addAttachments(attachmentsUri, new AttachmentInput(filename, in));
	}

	@Override
	public Promise<Void> addAttachments(final URI attachmentsUri, AttachmentInput... attachments) {
		// just to avoid concurrency issues if this arg is mutable
//		final ArrayList<AttachmentInput> myAttachments = Lists.newArrayList(attachments);
//		invoke(new Callable<Void>() {
//			@Override
//			public Void call() throws Exception {
//				final MultiPart multiPartInput = new MultiPart();
//				for (AttachmentInput attachment : myAttachments) {
//					BodyPart bp = new BodyPart(attachment.getInputStream(), MediaType.APPLICATION_OCTET_STREAM_TYPE);
//					FormDataContentDisposition.FormDataContentDispositionBuilder dispositionBuilder =
//							FormDataContentDisposition.name(FILE_ATTACHMENT_CONTROL_NAME);
//					dispositionBuilder.fileName(attachment.getFilename());
//					final FormDataContentDisposition formDataContentDisposition = dispositionBuilder.build();
//					bp.setContentDisposition(formDataContentDisposition);
//					multiPartInput.bodyPart(bp);
//				}
//
//				postFileMultiPart(multiPartInput, attachmentsUri);
//				return null;
//			}
//
//		});
        // todo: fix this
        throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Promise<InputStream> getAttachment(final URI attachmentUri) {
        return callAndParse(client.newRequest(attachmentUri).get(),
                new ResponseHandler<InputStream>()
                {
                    @Override
                    public InputStream handle(Response request) throws JSONException, IOException
                    {
                        return request.getEntityStream();
                    }
                });
	}

	@Override
	public Promise<Void> addAttachments(final URI attachmentsUri, File... files) {
//		final ArrayList<File> myFiles = Lists.newArrayList(files); // just to avoid concurrency issues if this arg is mutable
//		invoke(new Callable<Void>() {
//			@Override
//			public Void call() throws Exception {
//				final MultiPart multiPartInput = new MultiPart();
//				for (File file : myFiles) {
//					FileDataBodyPart fileDataBodyPart = new FileDataBodyPart(FILE_ATTACHMENT_CONTROL_NAME, file);
//					multiPartInput.bodyPart(fileDataBodyPart);
//				}
//				postFileMultiPart(multiPartInput, attachmentsUri);
//				return null;
//			}
//
//		});
        // fixme
        throw new UnsupportedOperationException("Not implemented yet");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Promise<Void> addComment(final URI commentsUri, final Comment comment) {
        return call(client.newRequest(commentsUri).setEntity(toEntity(new CommentJsonGenerator(getVersionInfo()), comment)).post());
	}
//
//	private void postFileMultiPart(MultiPart multiPartInput, URI attachmentsUri) {
//		final WebResource attachmentsResource = client.resource(attachmentsUri);
//		final WebResource.Builder builder = attachmentsResource.type(MultiPartMediaTypes.createFormData());
//		builder.header("X-Atlassian-Token", "nocheck"); // this is required by server side REST API
//		builder.post(multiPartInput);
//	}


	@Override
	public Promise<Void> watch(final URI watchersUri) {
		return addWatcher(watchersUri, null);
	}

	@Override
	public Promise<Void> unwatch(final URI watchersUri) {
		return removeWatcher(watchersUri, requestContext.getUserId());
	}

	@Override
	public Promise<BasicIssue> createIssue(IssueInput issue) {
		final UriBuilder uriBuilder = UriBuilder.fromUri(baseUri);
		uriBuilder.path("issue");
        return callAndParse(client.newRequest(uriBuilder.build()).setEntity(toEntity(
                new IssueInputJsonGenerator(), issue)).post(), basicIssueParser);
	}

	@Override
	public Promise<Iterable<CimProject>> getCreateIssueMetadata(@Nullable GetCreateIssueMetadataOptions options) {

		final UriBuilder uriBuilder = UriBuilder.fromUri(baseUri).path("issue/createmeta");

		if (options != null) {
			if (options.projectIds != null) {
				uriBuilder.queryParam("projectIds", Joiner.on(",").join(options.projectIds));
			}

			if (options.projectKeys != null) {
				uriBuilder.queryParam("projectKeys", Joiner.on(",").join(options.projectKeys));
			}

			if (options.issueTypeIds != null) {
				uriBuilder.queryParam("issuetypeIds", Joiner.on(",").join(options.issueTypeIds));
			}

			final Iterable<String> issueTypeNames = options.issueTypeNames;
			if (issueTypeNames != null) {
				for (final String name : issueTypeNames) {
					uriBuilder.queryParam("issuetypeNames", name);
				}
			}

			final Iterable<String> expandos = options.expandos;
			if (expandos != null && expandos.iterator().hasNext()) {
				uriBuilder.queryParam("expand", Joiner.on(",").join(expandos));
			}
		}

        return callAndParse(client.newRequest(uriBuilder.build()).get(),
                createIssueMetadataJsonParser);
	}

	@Override
	public Promise<Void> addWorklog(final URI worklogUri, final WorklogInput worklogInput) {
		final UriBuilder uriBuilder = UriBuilder.fromUri(worklogUri)
				.queryParam("adjustEstimate", worklogInput.getAdjustEstimate().restValue);

		switch (worklogInput.getAdjustEstimate()) {
			case NEW:
				uriBuilder.queryParam("newEstimate", Strings.nullToEmpty(worklogInput.getAdjustEstimateValue()));
				break;
			case MANUAL:
				uriBuilder.queryParam("reduceBy", Strings.nullToEmpty(worklogInput.getAdjustEstimateValue()));
				break;
		}

        return call(client.newRequest(uriBuilder.build()).setEntity(
                toEntity(new WorklogInputJsonGenerator(), worklogInput)).post());
	}
}
