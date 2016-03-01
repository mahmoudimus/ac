package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.WebHookModuleBeanBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A webhook is a standard mechanism for implementing HTTP callbacks. Atlassian-hosted cloud applications can execute
 * webhooks that your add-ons can use to be notified of certain events that happen in JIRA or Confluence.
 *
 * Just to give you an idea of how you can use them in add-ons, here are a few sample webhook events:
 *
 * <ul>
 *  <li>When an add-on is enabled or disabled</li>
 *  <li>When an issue is created or closed in JIRA</li>
 *  <li>When a page is created or updated in Confluence
 *
 * If your add-on uses webhooks, declare the ["read" scope](../../scopes/scopes.html)
 * in your [`atlassian-connect.json` descriptor](../).</li>
 * </ul>
 *
 * <h3>Handling the webhook event</h3>
 *
 *To receive webhook events, your add-on needs to include the webhook module declaration in its JSON descriptor. The
 * declaration indicates the relative URL of the local resource at which it will receive the notification. In other
 * words, the Atlassian application will send an HTTP POST to this resource in response to an application event. The
 * add-on code that handles the POST should process any information passed in the body of the message, as appropriate.
 * Each webhook POST sent to the add-on will also include the authentication headers that allow the add-on to validate
 * the authenticity of that request. Specifically, the JWT token can be found in the "Authorization" HTTP header.
 *
 *Note that if using Apache and mod_wsgi to serve files to a Django application, the Authentication header is stripped
 * out by default. [Extra configuration](http://www.django-rest-framework.org/api-guide/authentication/#apache-mod_wsgi-specific-configuration)
 * is required to ensure the Authentication header is visible.
 *
 * <h3>Variable Substitution</h3>
 *
 * JIRA webhooks also provide a way to add and substitute variables in the url. This is similar to context parameters for add-ons. See [context parameters](../../concepts/context-parameters.html).
 *
 * For example, if we register to listen for one of the project events with a url containing `${project.id}`, a POST message will
 * be sent to the address with the `${project.id}` replaced by the id of the project that the event was triggered for.
 *
 * <h3>Webhook event types</h3>
 *
 * Below is a list of all available webhook events.
 *
 * <h4>Add-on and system events</h4>
 * <ul>
 *  <li>`connect_addon_disabled`</li>
 *  <li>`connect_addon_enabled`</li>
 *  <li>`server_upgraded`</li>
 * </ul>
 *
 * <h4>Issue events</h4>
 * <ul>
 *  <li>`jira:issue_created`</li>
 *  <li>`jira:issue_deleted`</li>
 *  <li>`jira:issue_updated`</li>
 *  <li>`jira:worklog_updated`
 *
 * Context parameters are `${project.id}, ${project.key}, ${issue.key}, ${issue.id}`</li>
 * </ul>
 *
 * <h4>Version events</h4>
 * <ul>
 *  <li>`version_created`</li>
 *  <li>`version_deleted`</li>
 *  <li>`version_merged`</li>
 *  <li>`version_updated`</li>
 *  <li>`version_moved`</li>
 *  <li>`version_released`</li>
 *  <li>`version_unreleased`
 *
 * Context parameters are `${project.id}, ${project.key}, ${version.id}`.
 *
 * Special context parameter for version_merged event is `${mergedVersion.id}`.</li>
 * </ul>
 *
 * <h4>Project events</h4>
 * <ul>
 *  <li>`project_created`</li>
 *  <li>`project_updated`</li>
 *  <li>`project_deleted`
 *
 * Context parameters are `${project.id}, ${project.key}`</li>
 * </ul>
 *
 * <h4>User events</h4>
 * <ul>
 *  <li>`user_created`</li>
 *  <li>`user_deleted`</li>
 *  <li>`user_updated`
 *
 * Context parameters: `${modifiedUser.name}, ${modifiedUser.key}`</li>
 * </ul>
 *
 * <h4>Feature status events</h4>
 * <ul>
 *  <li>`option_voting_changed`</li>
 *  <li>`option_watching_changed`</li>
 *  <li>`option_unassigned_issues_changed`</li>
 *  <li>`option_subtasks_changed`</li>
 *  <li>`option_attachments_changed`</li>
 *  <li>`option_issuelinks_changed`</li>
 *  <li>`option_timetracking_changed`</li>
 * </ul>
 *
 * <h4>Confluence Webhook events</h4>
 *
 * <ul>
 *  <li>`attachment_created`</li>
 *  <li>`attachment_removed`</li>
 *  <li>`attachment_updated`</li>
 *  <li>`attachment_viewed`</li>
 *  <li>`blog_created`</li>
 *  <li>`blog_removed`</li>
 *  <li>`blog_restored`</li>
 *  <li>`blog_trashed`</li>
 *  <li>`blog_updated`</li>
 *  <li>`blog_viewed`</li>
 *  <li>`blueprint_page_created`</li>
 *  <li>`cache_statistics_changed`</li>
 *  <li>`comment_created`</li>
 *  <li>`comment_removed`</li>
 *  <li>`comment_updated`</li>
 *  <li>`connect_addon_disabled`</li>
 *  <li>`connect_addon_enabled`</li>
 *  <li>`content_permissions_updated`</li>
 *  <li>`group_created`</li>
 *  <li>`group_removed`</li>
 *  <li>`label_added`</li>
 *  <li>`label_created`</li>
 *  <li>`label_deleted`</li>
 *  <li>`label_removed`</li>
 *  <li>`login`</li>
 *  <li>`login_failed`</li>
 *  <li>`logout`</li>
 *  <li>`page_children_reordered`</li>
 *  <li>`page_created`</li>
 *  <li>`page_moved`</li>
 *  <li>`page_removed`</li>
 *  <li>`page_restored`</li>
 *  <li>`page_trashed`</li>
 *  <li>`page_updated`</li>
 *  <li>`page_viewed`</li>
 *  <li>`plugin_enabled`</li>
 *  <li>`plugins_upgraded`</li>
 *  <li>`search_performed`</li>
 *  <li>`server_upgraded`</li>
 *  <li>`space_created`</li>
 *  <li>`space_logo_updated`</li>
 *  <li>`space_permissions_updated`</li>
 *  <li>`space_removed`</li>
 *  <li>`space_updated`</li>
 *  <li>`user_created`</li>
 *  <li>`user_deactivated`</li>
 *  <li>`user_followed`</li>
 *  <li>`user_reactivated`</li>
 *  <li>`user_removed`</li>
 * </ul>
 *
 * <h4>Example Request</h4>
 *
 * <pre><code>
 *POST /jira-issue_created?user_id=admin&amp;user_key=admin HTTP/1.1
 *Authorization: JWT ...
 *Atlassian-Connect-Version: x.x
 *Content-Type: application/json
 *{
 *  timestamp: 1426661049725,
 *  webhookEvent: 'jira:issue_created',
 *  ...
 *}
 * </code></pre>
 *
 *### Inspecting webhook contents
 *
 * Each type of webhook event includes information specific to that event in the body content of the POST message. The
 * add-on resource that listens for webhook posts should receive and process the content as appropriate for the add-on.
 * To understand what type of content each webhook generates, you can use the webhook inspector tool.
 *
 * The [Webhook Inspector](https://bitbucket.org/atlassianlabs/webhook-inspector) is a
 * [atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express) Connect add-on
 * that you can install in your development environment to inspect the content of event messages. The Webhook Inspector
 * subscribes and generates each webhook event type available on the running instance of the Atlassian application,
 * and prints the body posted by the instance to the console screen.
 *
 *### References
 * <ul>
 *  <li>[JIRA Webhooks: What will the format of the webhook callback message be?](https://developer.atlassian.com/jiradev/jira-architecture/webhooks#Webhooks-Whatwilltheformatofthewebhookcallbackmessagebe%3F)</li>
 *  <li>[JIRA Webhooks: Sample Webhook POST](https://developer.atlassian.com/jiradev/jira-architecture/webhooks#Webhooks-SampleWebhookPOST)</li>
 * </ul>
 *
 *### Tutorials
 * <ul>
 *  <li>[Confluence Webhooks: Writing a Multi-page Blueprint using Atlassian Connect](https://developer.atlassian.com/confdev/tutorials/writing-a-multi-page-blueprint-using-atlassian-connect)</li>
 * </ul>
 *
 * <h2>Example</h2>
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#WEBHOOK_EXAMPLE}
 * @schemaTitle Webhook
 * @since 1.0
 */
@SchemaDefinition("webhook")
public class WebHookModuleBean extends BeanWithParams {
    /**
     * Specifies the named event you would like to listen to (e.g., "enabled", "jira:issue_created", etc.)
     */
    @Required
    private String event;

    /**
     * Specifies your add-on's POST webhook handler URL. This property must be a URL relative to the add-on's baseUrl. 
     */
    @Required
    @StringSchemaAttributes(format = "uri-template")
    private String url;

    public WebHookModuleBean(WebHookModuleBeanBuilder builder) {
        super(builder);

        if (null == event) {
            this.event = "";
        }

        if (null == url) {
            this.url = "";
        }

    }

    protected WebHookModuleBean() {
        this.event = "";
        this.url = "";
    }

    public String getEvent() {
        return event;
    }

    public String getUrl() {
        return url;
    }

    public static WebHookModuleBeanBuilder newWebHookBean() {
        return new WebHookModuleBeanBuilder();
    }

    public static WebHookModuleBeanBuilder newWebHookBean(WebHookModuleBean defaultBean) {
        return new WebHookModuleBeanBuilder(defaultBean);
    }

    @Override
    public boolean equals(Object otherObj) {
        if (otherObj == this) {
            return true;
        }

        if (!(otherObj instanceof WebHookModuleBean && super.equals(otherObj))) {
            return false;
        }

        WebHookModuleBean other = (WebHookModuleBean) otherObj;

        return new EqualsBuilder()
                .append(url, other.url)
                .append(event, other.event)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 61)
                .appendSuper(super.hashCode())
                .append(url)
                .append(event)
                .build();
    }
}
