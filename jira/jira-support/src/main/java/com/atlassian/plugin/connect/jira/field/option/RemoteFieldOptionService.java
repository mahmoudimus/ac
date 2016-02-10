package com.atlassian.plugin.connect.jira.field.option;

import java.util.List;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.plugin.connect.jira.field.FieldId;
import org.codehaus.jackson.JsonNode;

/**
 * Service for accessing and manipulating available options storage for select custom fields.
 */
public interface RemoteFieldOptionService
{
    /**
     * Create a new option.
     *
     * @param fieldId id of the field the option will belong to
     * @param value value of the new option
     * @return the created option if successful, errors otherwise
     */
    ServiceOutcome<RemoteFieldOption> addOption(FieldId fieldId, JsonNode value);

    /**
     * Updates an option with the new value. The id of the option to update is taken from the {@code option} parameter.
     *
     * @param fieldId the field the option belongs to
     * @param option the new option
     * @return the updated option if successful, errors if the option to update does not exist
     */
    ServiceOutcome<RemoteFieldOption> updateOption(FieldId fieldId, RemoteFieldOption option);

    /**
     * Get all options for a specific field.
     *
     * @param fieldId the field to get the options for
     * @return a list of all options
     */
    ServiceOutcome<List<RemoteFieldOption>> getAllOptions(FieldId fieldId);

    /**
     * Get an option with a particular id for a specified field
     *
     * @param fieldId the field the option belongs to
     * @param optionId the option id
     * @return the option or errors if not found
     */
    ServiceOutcome<RemoteFieldOption> getOption(FieldId fieldId, Integer optionId);

    /**
     * Delete a specified option
     *
     * @param fieldId the field the option belongs to
     * @param optionId the option id
     * @return a successful result regardless of whether the option existed or not. Errors in case of server problems, DB access or similar
     */
    ServiceResult removeOption(FieldId fieldId, Integer optionId);

    /**
     * Reset the {@code from} value to the {@code to} value in all issues that have the field
     * specified by the {@code fieldId} parameter set to {@code from}.
     *
     * <p>
     *     Useful when you wish to {@link RemoteFieldOptionService#removeOption(FieldId, Integer)} an option but there are
     *     still some issues that have the option assigned.
     * </p>
     *
     * @param fieldId the field the option belongs to
     * @param from the value currently set in issues
     * @param to the value to replace the previous value with
     * @return a successful result regardless of how many issues were affected. Errors in case of server problems, DB access or similar
     */
    ServiceResult replaceInAllIssues(FieldId fieldId, Integer from, Integer to);
}
