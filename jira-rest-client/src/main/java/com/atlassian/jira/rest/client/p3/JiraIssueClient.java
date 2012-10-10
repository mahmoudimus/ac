package com.atlassian.jira.rest.client.p3;

import com.atlassian.jira.rest.client.GetCreateIssueMetadataOptions;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.CimProject;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.Votes;
import com.atlassian.jira.rest.client.domain.Watchers;
import com.atlassian.jira.rest.client.domain.input.AttachmentInput;
import com.atlassian.jira.rest.client.domain.input.IssueInput;
import com.atlassian.jira.rest.client.domain.input.LinkIssuesInput;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.domain.input.WorklogInput;
import com.atlassian.util.concurrent.Promise;
import com.google.common.annotations.Beta;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.net.URI;

public interface JiraIssueClient
{
	/**
	 * Creates new issue.
	 *
	 * @param issue		   populated with data to create new issue
	 * @return basicIssue with generated <code>issueKey</code>
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, etc.)
	 * @since client 1.0, server 5.0
	 */
	Promise<BasicIssue> createIssue(IssueInput issue);

	/**
	 * Retrieves CreateIssueMetadata with specified filters.
	 *
	 * @param options		  optional request configuration like filters and expandos. You may use {@link com.atlassian.jira.rest.client.GetCreateIssueMetadataOptionsBuilder} to build them. Pass <code>null</code> if you don't want to set any option.
	 * @return List of {@link com.atlassian.jira.rest.client.domain.CimProject} describing projects, issue types and fields.
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, etc.)
	 * @since client 1.0, server 5.0
	 */
    Promise<Iterable<CimProject>> getCreateIssueMetadata(@Nullable GetCreateIssueMetadataOptions options);

	/**
	 * Retrieves issue with selected issue key.
	 *
	 * @param issueKey issue key (like TST-1, or JRA-9)
	 * @return issue with given <code>issueKey</code>
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, etc.)
	 */
    Promise<Issue> getIssue(String issueKey);

	/**
	 * Retrieves issue with selected issue key, with specified additional expandos.
	 *
	 * @param issueKey issue key (like TST-1, or JRA-9)
	 * @param expand additional expands. Currently CHANGELOG is the only supported expand that is not expanded by default.
	 * @return issue with given <code>issueKey</code>
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, etc.)
	 * @since 0.6
	 */
    Promise<Issue> getIssue(String issueKey, Iterable<Expandos> expand);

	/**
	 * Retrieves complete information (if the caller has permission) about watchers for selected issue.
	 *
	 * @param watchersUri URI of watchers resource for selected issue. Usually obtained by calling <code>Issue.getWatchers().getSelf()</code>
	 * @return detailed information about watchers watching selected issue.
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, etc.)
	 * @see com.atlassian.jira.rest.client.domain.Issue#getWatchers()
	 */
    Promise<Watchers> getWatchers(URI watchersUri);

	/**
	 * Retrieves complete information (if the caller has permission) about voters for selected issue.
	 *
	 * @param votesUri URI of voters resource for selected issue. Usually obtained by calling <code>Issue.getVotesUri()</code>
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, etc.)

	 * @return detailed information about voters of selected issue
	 * @see com.atlassian.jira.rest.client.domain.Issue#getVotesUri()
	 */
    Promise<Votes> getVotes(URI votesUri);

	/**
	 * Retrieves complete information (if the caller has permission) about transitions available for the selected issue in its current state.
	 *
	 * @param transitionsUri URI of transitions resource of selected issue. Usually obtained by calling <code>Issue.getTransitionsUri()</code>
	 * @return transitions about transitions available for the selected issue in its current state.
     *
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, etc.)
	 */
    Promise<Iterable<Transition>> getTransitions(URI transitionsUri);

	/**
	 * Retrieves complete information (if the caller has permission) about transitions available for the selected issue in its current state.
	 *
	 * @since v0.5
	 * @param issue issue
	 * @return transitions about transitions available for the selected issue in its current state.
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, etc.)
	 */
    Promise<Iterable<Transition>> getTransitions(Issue issue);

	/**
	 * Performs selected transition on selected issue.
	 * @param transitionsUri URI of transitions resource of selected issue. Usually obtained by calling <code>Issue.getTransitionsUri()</code>
	 * @param transitionInput data for this transition (fields modified, the comment, etc.)
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, etc.)

	 */
    Promise<Void> transition(URI transitionsUri, TransitionInput transitionInput);

	/**
	 * Performs selected transition on selected issue.
	 * @since v0.5
	 * @param issue selected issue
	 * @param transitionInput data for this transition (fields modified, the comment, etc.)
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, etc.)

	 */
    Promise<Void> transition(Issue issue, TransitionInput transitionInput);

	/**
	 * Casts your vote on the selected issue. Casting a vote on already votes issue by the caller, causes the exception.
	 * @param votesUri URI of votes resource for selected issue. Usually obtained by calling <code>Issue.getVotesUri()</code>
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, etc.)
	 */
    Promise<Void> vote(URI votesUri);

	/**
	 * Removes your vote from the selected issue. Removing a vote from the issue without your vote causes the exception.
	 * @param votesUri URI of votes resource for selected issue. Usually obtained by calling <code>Issue.getVotesUri()</code>
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, etc.)
	 */
    Promise<Void> unvote(URI votesUri);

	/**
	 * Starts watching selected issue
	 * @param watchersUri URI of watchers resource for selected issue. Usually obtained by calling <code>Issue.getWatchers().getSelf()</code>
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, etc.)
	 */
    Promise<Void> watch(URI watchersUri);

	/**
	 * Stops watching selected issue
	 * @param watchersUri URI of watchers resource for selected issue. Usually obtained by calling <code>Issue.getWatchers().getSelf()</code>
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, etc.)
	 */
    Promise<Void> unwatch(URI watchersUri);

	/**
	 * Adds selected person as a watcher for selected issue. You need to have permissions to do that (otherwise
	 * the exception is thrown).
	 *
	 * @param watchersUri URI of watchers resource for selected issue. Usually obtained by calling <code>Issue.getWatchers().getSelf()</code>
	 * @param username user to add as a watcher
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, etc.)
	 */
    Promise<Void> addWatcher(final URI watchersUri, final String username);

	/**
	 * Removes selected person from the watchers list for selected issue. You need to have permissions to do that (otherwise
	 * the exception is thrown).
	 *
	 * @param watchersUri URI of watchers resource for selected issue. Usually obtained by calling <code>Issue.getWatchers().getSelf()</code>
	 * @param username user to remove from the watcher list
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, etc.)
	 */
    Promise<Void> removeWatcher(final URI watchersUri, final String username);

	/**
	 * Creates link between two issues and adds a comment (optional) to the source issues.
	 *
	 * @param linkIssuesInput details for the link and the comment (optional) to be created
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, invalid argument, permissions, etc.)
	 * @since client 0.2, server 4.3
	 */
    Promise<Void> linkIssue(LinkIssuesInput linkIssuesInput);

	/**
	 * Uploads attachments to JIRA (adding it to selected issue)
	 *
	 * @param attachmentsUri where to upload the attachment. You can get this URI by examining issue resource first
	 * @param in stream from which to read data to upload
	 * @param filename file name to use for the uploaded attachment
	 * @since client 0.2, server 4.3
	 */
    Promise<Void> addAttachment(URI attachmentsUri, InputStream in,
            String filename);

	/**
	 * Uploads attachments to JIRA (adding it to selected issue)
	 *
	 * @param attachmentsUri where to upload the attachments. You can get this URI by examining issue resource first
	 * @param attachments attachments to upload
	 * @since client 0.2, server 4.3
	 */
    Promise<Void> addAttachments(URI attachmentsUri,
            AttachmentInput... attachments);

	/**
	 * Uploads attachments to JIRA (adding it to selected issue)
	 * @param attachmentsUri where to upload the attachments. You can get this URI by examining issue resource first
	 * @param files files to upload
	 * @since client 0.2, server 4.3
	 */
    Promise<Void> addAttachments(URI attachmentsUri, File... files);

	/**
	 * Adds a comment to JIRA (adding it to selected issue)
	 * @param commentsUri where to add comment
	 * @param comment the {@link com.atlassian.jira.rest.client.domain.Comment} to add
	 * @since client 1.0, server 5.0
	 */
    Promise<Void> addComment(URI commentsUri, Comment comment);

	/**
	 * Retrieves the content of given attachment.
	 *
	 *
	 * @param attachmentUri URI for the attachment to retrieve
	 * @return stream from which the caller may read the attachment content (bytes). The caller is responsible for closing the stream.
	 */
	@Beta
	Promise<InputStream> getAttachment(URI attachmentUri);

	/**
	 * Adds new worklog entry to issue.
	 *
	 * @param worklogUri	  URI for worklog in issue
	 * @param worklogInput	worklog input object to create
	 */
    Promise<Void> addWorklog(URI worklogUri, WorklogInput worklogInput);

	/**
	 * Expandos supported by {@link com.atlassian.jira.rest.client.p3.JiraIssueClient#getIssue(String, Iterable)}
	 */
	public enum Expandos {
		CHANGELOG, SCHEMA, NAMES, TRANSITIONS
	}
}
