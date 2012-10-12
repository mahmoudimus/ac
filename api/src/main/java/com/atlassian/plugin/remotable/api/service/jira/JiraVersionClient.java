package com.atlassian.plugin.remotable.api.service.jira;

import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.VersionRelatedIssuesCount;
import com.atlassian.jira.rest.client.domain.input.VersionInput;
import com.atlassian.jira.rest.client.domain.input.VersionPosition;
import com.atlassian.util.concurrent.Promise;

import javax.annotation.Nullable;
import java.net.URI;

/**
 * The client responsible for Project version(s) related operations
 */
public interface JiraVersionClient
{

	/**
	 * Retrieves full information about selected project version
	 *
	 * @param versionUri URI of the version to retrieve. You can get it for example from Project or it can be
	 *        referenced from an issue.
	 * @return full information about selected project version
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
    Promise<Version> getVersion(URI versionUri);

	/**
	 * Creates a new version (which logically belongs to a project)
	 *
	 * @param version details about version to create
	 * @return newly created version
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
    Promise<Version> createVersion(VersionInput version);

	/**
	 * Updates selected version with a new details.
	 *
	 * @param versionUri full URI to the version to update
	 * @param versionInput new details of the version. <code>null</code> fields will be ignored
	 * @return newly updated version
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
    Promise<Version> updateVersion(URI versionUri, VersionInput versionInput);

	/**
	 * Removes selected version optionally changing Fix Version(s) and/or Affects Version(s) fields of related issues.
	 *
	 * @param versionUri full URI to the version to remove
	 * @param moveFixIssuesToVersionUri URI of the version to which issues should have now set their Fix Version(s)
	 *        field instead of the just removed version. Use <code>null</code> to simply clear Fix Version(s) in all those issues
	 *        where the version removed was referenced.
	 * @param moveAffectedIssuesToVersionUri URI of the version to which issues should have now set their Affects Version(s)
	 *        field instead of the just removed version. Use <code>null</code> to simply clear Affects Version(s) in all those issues
	 *        where the version removed was referenced.
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 */
    Promise<Void> removeVersion(URI versionUri, @Nullable URI moveFixIssuesToVersionUri,
            @Nullable URI moveAffectedIssuesToVersionUri);

	/**
	 * Retrieves basic statistics about issues which have their Fix Version(s) or Affects Version(s) field
	 * pointing to given version.
	 *
	 * @param versionUri full URI to the version you want to get related issues count for
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 * @return basic stats about issues related to given version
	 */
    Promise<VersionRelatedIssuesCount> getVersionRelatedIssuesCount(URI versionUri);

	/**
	 * Retrieves number of unresolved issues which have their Fix Version(s) field
	 * pointing to given version.
	 *
	 * @param versionUri full URI to the version you want to get the number of unresolved issues for
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 * @return number of unresolved issues having this version included in their Fix Version(s) field.
     *
	 */
    Promise<Integer> getNumUnresolvedIssues(URI versionUri);

	/**
	 * Moves selected version after another version. Ordering of versions is important on various reports and whenever
	 * input version fields are rendered by JIRA.
	 * If version is already immediately after the other version (defined by <code>afterVersionUri</code>) then
	 * such call has no visual effect.
	 *
	 * @param versionUri full URI to the version to move
	 * @param afterVersionUri URI of the version to move selected version after
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 * @return just moved version
	 */
    Promise<Version> moveVersionAfter(URI versionUri, URI afterVersionUri);

	/**
	 * Moves selected version to another position.
	 * If version already occupies given position (e.g. is the last version and we want to move to a later position or to the last position)
	 * then such call does not change anything.
	 *
	 * @param versionUri full URI to the version to move
	 * @param versionPosition defines a new position of selected version
	 * @throws com.atlassian.jira.rest.client.RestClientException in case of problems (connectivity, malformed messages, etc.)
	 * @return just moved version
	 */
    Promise<Version> moveVersion(URI versionUri, VersionPosition versionPosition);

}
