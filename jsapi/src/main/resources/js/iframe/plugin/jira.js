AP.define("jira", ["_dollar", "_rpc"], function ($, rpc) {
    "use strict";
    var workflowListener;
    var validationListener;
    var dashboardItemEditListener;
    var issueCreateListener;
    var dateSelectedListener;

    /**
     * @class WorkflowConfiguration
     */
    var WorkflowConfiguration = {
        /**
         * Validate a workflow configuration before saving
         * @noDemo
         * @memberOf WorkflowConfiguration
         * @param {Function} listener called on validation. Return false to indicate that validation has not passed and the workflow cannot be saved.
         */
        onSaveValidation: function (listener) {
            validationListener = listener;
        },
        /**
         * Attach a callback function to run when a workflow is saved
         * @noDemo
         * @memberOf WorkflowConfiguration
         * @param {Function} listener called on save.
         */
        onSave: function (listener) {
            workflowListener = listener;
        },
        /**
         * Save a workflow configuration if valid.
         * @noDemo
         * @memberOf WorkflowConfiguration
         * @returns {WorkflowConfigurationTriggerResponse} An object Containing `{valid, value}` properties.valid (the result of the validation listener) and value (result of onSave listener) properties.
         */
        trigger: function () {
            var valid = true;
            if ($.isFunction(validationListener)) {
                valid = validationListener.call();
            }
            /**
             * An object returned when the {@link WorkflowConfiguration} trigger method is invoked.
             * @name WorkflowConfigurationTriggerResponse
             * @class
             * @property {Boolean} valid The result of the validation listener {@link WorkflowConfiguration.onSaveValidation}
             * @property {*} value The result of the {@link WorkflowConfiguration.onSave}
             */
            return {
                valid: valid,
                value: valid ? "" + workflowListener.call() : undefined
            };
        }
    };

    /**
     * @class DashboardItem
     */
    var DashboardItem = {
        /**
         * Attach a callback function to run when user clicks 'edit' in the dashboard item's menu
         * @noDemo
         * @memberOf DashboardItem
         * @param {Function} listener called on dashboard item edit.
         */
        onDashboardItemEdit: function (listener) {
            dashboardItemEditListener = listener;
        },
        triggerEdit: function () {
            if ($.isFunction(dashboardItemEditListener)) {
                dashboardItemEditListener.call();
            }
        }
    };

    var apis = rpc.extend(function (remote) {
        return {

            /**
             * A Javascript module which provides functions to interact with JIRA.
             * @see {WorkflowConfiguration}
             * @see {DashboardItem}
             *
             * @exports jira
             */
            apis: {
                /**
                 * Retrieves a workflow configuration object.
                 *
                 * @param {WorkflowConfiguration} callback - the callback that handles the response.
                 */
                getWorkflowConfiguration: function (callback) {
                    remote.getWorkflowConfiguration(callback);
                },

                /**
                 * Set the title of a dashboard item to the given text.
                 *
                 * @param {String} title - the title of the dashboard item. Any HTML is escaped.
                 */
                setDashboardItemTitle: function (title) {
                    remote.setDashboardItemTitle(title);
                },

                /**
                 * Returns whether the current user is permitted to edit the dashboard item
                 *
                 * @param {Function} callback - the callback that handles the response
                 */
                isDashboardItemEditable: function (callback) {
                    remote.isDashboardItemEditable(callback);
                },

                /**
                 * Refresh an issue page without reloading the browser.
                 *
                 * This is helpful when your add-on updates information about an issue in the background.
                 * @noDemo
                 * @example
                 * AP.require('jira', function(jira){
                 *   jira.refreshIssuePage();
                 * });
                 */
                refreshIssuePage: function () {
                    remote.triggerJiraEvent('refreshIssuePage');
                },

                /**
                 * Open the quick create issue dialog. The dialog fields may be pre-filled with supplied data. A callback will be invoked when the dialog is closed and will include an array of issues created.
                 *
                 * @param {Function} callback - invoked when dialog is closed, takes a single parameter - array of issues created
                 * @param {Object} fields - contains data to pre-fill the dialog with
                 * @param {number} fields.pid - Project to pre-fill the dialog with
                 * @param {number} fields.issueType - Issue type to pre-fill the dialog with
                 * @noDemo
                 * @example
                 * AP.require('jira', function(jira){
                 *   jira.openCreateIssueDialog(function(issues){
                 *       alert(issues[0]['fields']['summary']);
                 *   } , {
                 *       pid: 10000,
                 *       issueType: 1
                 *   });
                 * });
                 */
                openCreateIssueDialog: function (callback, fields) {
                    issueCreateListener = callback || null;
                    remote.openCreateIssueDialog(fields);
                },

                /**
                 * @class DatePicker~position
                 * @property {number} top - Distance in pixels from the top edge of the iframe date picker should be shown at.
                 * @property {number} left - Distance in pixels from the left edge of the iframe date picker should be shown at.
                 */

                /**
                 * @class DatePicker~options
                 * @property {HTMLElement} element - HTML element below which date picker will be positioned. If provided, it takes precedence over `options.position`.
                 * @property {DatePicker~position} position - Position of the element relative to the iframe. options.element takes precedence over it when provided.
                 * @property {Boolean} showTime - Flag determining whether the component should also have a time picker. Defaults to `false`.
                 * @property {String} date - <p>Date (and time) that should be pre-selected when displaying the picker in the format understandable by Date.parse method in JavaScript.</p>
                 * <p>ISO 8601 is preferred. Timezone should be set to Z for UTC time or in the format of +/-hh:mm. Not setting it will cause JavaScript to use local timezone set in the browser. Defaults to current date/time.</p>
                 * @property {Function} onSelect - Callback that will be invoked when the date (and time) is selected by the user.
                 */

                /**
                 * Shows a date picker component. A callback will be invoked when the date (and time) is selected by the user.
                 *
                 * @param {DatePicker~options} options - Configuration of the date picker.
                 *
                 * @noDemo
                 * @example
                 * AP.require('jira', function(jira){
                 *     var dateField = document.querySelector("#date-field");
                 *     var dateTrigger = document.querySelector("#date-trigger");
                 *
                 *     dateTrigger.addEventListener("click", function(e) {
                 *         e.preventDefault();
                 *         jira.openDatePicker({
                 *             element: dateTrigger,
                 *             date: "2011-12-13T15:20+01:00",
                 *             showTime: true,
                 *             onSelect: function (isoDate, date) {
                 *                 dateField.value = date;
                 *                 dateField.setAttribute("data-iso", isoDate);
                 *                 dateField.focus();
                 *             }
                 *         });
                 *     });
                 * });
                 */
                openDatePicker: function (options) {
                    function isDomElement(el) {
                        return el && el.nodeType && el.nodeType == 1;
                    }

                    options = options || {};
                    if (!options.position || typeof options.position !== "object") {
                        if (!isDomElement(options.element)) {
                            throw new Error("Providing either options.position or options.element is required.");
                        }
                        options.position = {}
                    }

                    if (!options.onSelect || typeof options.onSelect !== "function") {
                        throw new Error("options.onSelect function is a required parameter.");
                    }

                    var sanitisedOptions = {
                        element: options.element,
                        position: {
                            top: options.position.top || 0,
                            left: options.position.left || 0
                        },
                        date: options.date,
                        showTime: !!options.showTime,
                        onSelect: options.onSelect
                    };

                    var elBoundingBox;
                    dateSelectedListener = sanitisedOptions.onSelect;
                    delete sanitisedOptions.onSelect;

                    if (sanitisedOptions.element) {
                        elBoundingBox = sanitisedOptions.element.getBoundingClientRect();
                        sanitisedOptions.position = {
                            left: elBoundingBox.left,
                            top: elBoundingBox.top + elBoundingBox.height
                        };
                        delete sanitisedOptions.element;
                    }

                    remote.openDatePicker(sanitisedOptions);
                }
            },

            internals: {
                triggerDateSelectedListener: function (date, isoDate) {
                    if ($.isFunction(dateSelectedListener)) {
                        dateSelectedListener.call({}, date, isoDate);
                    }
                },
                setWorkflowConfigurationMessage: function () {
                    return WorkflowConfiguration.trigger();
                },
                triggerDashboardItemEdit: function () {
                    return DashboardItem.triggerEdit();
                },
                triggerIssueCreateSubmit: function (issues) {
                    if ($.isFunction(issueCreateListener)) {
                        issueCreateListener.call({}, issues);
                    }
                }
            },
            stubs: ["triggerJiraEvent", "openCreateIssueDialog"]

        };

    });

    return $.extend(apis, {
        WorkflowConfiguration: WorkflowConfiguration,
        DashboardItem: DashboardItem
    });

});
