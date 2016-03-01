package com.atlassian.plugin.connect.jira.field.option;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.util.Page;
import com.atlassian.jira.util.PageRequest;
import com.atlassian.plugin.connect.api.auth.AuthenticationData;
import com.atlassian.plugin.connect.jira.field.FieldId;
import org.codehaus.jackson.JsonNode;

/**
 * Service for accessing and manipulating available options storage for select custom fields.
 *
 * <p>
 *     Only system admin or add-on that added the field can access any of these methods.
 *     An appropriate error will be returned by each method if authentication fails.
 * </p>
 */
public interface ConnectFieldOptionService {
    /**
     * Create a new option.
     *
     * @param fieldId id of the field the option will belong to
     * @param value value of the new option
     * @param auth authentication details of the current call
     * @return the created option if successful, errors otherwise
     */
    ServiceOutcome<ConnectFieldOption> addOption(AuthenticationData auth, FieldId fieldId, JsonNode value);

    /**
     * Put the specified option into the database. If an option with the same id already exists, it will be replaced.
     *
     * @param fieldId the field the option belongs to
     * @param option the option to be saved in the database
     * @return the updated option if successful, errors otherwise
     */
    ServiceOutcome<ConnectFieldOption> putOption(AuthenticationData auth, FieldId fieldId, ConnectFieldOption option);

    /**
     * Get a page of options for a specific field.
     *
     * @param fieldId the field to get the options for
     * @param auth authentication details of the current call
     * @param pageRequest requested page
     * @return a page of all options
     */
    ServiceOutcome<Page<ConnectFieldOption>> getOptions(AuthenticationData auth, FieldId fieldId, PageRequest pageRequest);

    /**
     * Get an option with a particular id for a specified field
     *
     * @param fieldId the field the option belongs to
     * @param optionId the option id
     * @param auth authentication details of the current call
     * @return the option or errors if not found
     */
    ServiceOutcome<ConnectFieldOption> getOption(AuthenticationData auth, FieldId fieldId, Integer optionId);

    /**
     * Delete a specified option
     *
     * @param fieldId the field the option belongs to
     * @param optionId the option id
     * @param auth authentication details of the current call
     * @return a successful result regardless of whether the option existed or not. Errors in case of server problems, DB access or similar
     */
    ServiceResult removeOption(AuthenticationData auth, FieldId fieldId, Integer optionId);

    /**
     * Reset the {@code from} value to the {@code to} value in all issues that have the field
     * specified by the {@code fieldId} parameter set to {@code from}.
     *
     * <p>
     *     Useful when you wish to {@link ConnectFieldOptionService#removeOption(AuthenticationData, FieldId, Integer)} an option but there are
     *     still some issues that have the option assigned.
     * </p>
     *
     * @param fieldId the field the option belongs to
     * @param from the value currently set in issues
     * @param to the value to replace the previous value with
     * @param auth authentication details of the current call
     * @return a successful result regardless of how many issues were affected. Errors in case of server problems, DB access or similar
     */
    ServiceResult replaceInAllIssues(AuthenticationData auth, FieldId fieldId, Integer from, Integer to);
}
