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
 * If your add-on uses webhooks, declare the <a href="../../scopes/scopes.html">"read" scope</a>
 * in your <a href="../"><code>atlassian-connect.json</code> descriptor</a>.</li>
 * </ul>
 *
 * <h3>Handling the webhook event</h3>
 *
 * To receive webhook events, your add-on needs to include the webhook module declaration in its JSON descriptor. The
 * declaration indicates the relative URL of the local resource at which it will receive the notification. In other
 * words, the Atlassian application will send an HTTP POST to this resource in response to an application event. The
 * add-on code that handles the POST should process any information passed in the body of the message, as appropriate.
 * Each webhook POST sent to the add-on will also include the authentication headers that allow the add-on to validate
 * the authenticity of that request. Specifically, the JWT token can be found in the "Authorization" HTTP header.
 *
 * Note that if using Apache and mod_wsgi to serve files to a Django application, the Authentication header is stripped
 * out by default. <a href="http://www.django-rest-framework.org/api-guide/authentication/#apache-mod_wsgi-specific-configuration">Extra configuration</a>
 * is required to ensure the Authentication header is visible.
 *
 * <h3>Variable Substitution</h3>
 *
 * JIRA webhooks also provide a way to add and substitute variables in the url. This is similar to context parameters for add-ons. See <a href="../../concepts/context-parameters.html">context parameters</a>.
 *
 * For example, if we register to listen for one of the project events with a url containing <code>${project.id}</code>, a POST message will
 * be sent to the address with the <code>${project.id}</code> replaced by the id of the project that the event was triggered for.
 *
 * <h3>Webhook event types</h3>
 *
 * Below is a list of all available webhook events.
 *
 * <h4>Add-on and system events</h4>
 * <ul>
 *  <li><code>connect_addon_disabled</code></li>
 *  <li><code>connect_addon_enabled</code></li>
 *  <li><code>server_upgraded</code></li>
 * </ul>
 *
 * <h4>Issue events</h4>
 * <ul>
 *  <li><code>jira:issue_created</code></li>
 *  <li><code>jira:issue_deleted</code></li>
 *  <li><code>jira:issue_updated</code></li>
 *  <li><code>jira:worklog_updated</code>
 *
 * Context parameters are <code>${project.id}, ${project.key}, ${issue.key}, ${issue.id}</code></li>
 * </ul>
 *
 * <h4>Version events</h4>
 * <ul>
 *  <li><code>version_created</code></li>
 *  <li><code>version_deleted</code></li>
 *  <li><code>version_merged</code></li>
 *  <li><code>version_updated</code></li>
 *  <li><code>version_moved</code></li>
 *  <li><code>version_released</code></li>
 *  <li><code>version_unreleased</code>
 *
 * Context parameters are <code>${project.id}, ${project.key}, ${version.id}</code>.
 *
 * Special context parameter for version_merged event is <code>${mergedVersion.id}</code>.</li>
 * </ul>
 *
 * <h4>Project events</h4>
 * <ul>
 *  <li><code>project_created</code></li>
 *  <li><code>project_updated</code></li>
 *  <li><code>project_deleted</code>
 *
 * Context parameters are <code>${project.id}, ${project.key}</code></li>
 * </ul>
 *
 * <h4>User events</h4>
 * <ul>
 *  <li><code>user_created</code></li>
 *  <li><code>user_deleted</code></li>
 *  <li><code>user_updated</code>
 *
 * Context parameters: <code>${modifiedUser.name}, ${modifiedUser.key}</code></li>
 * </ul>
 *
 * <h4>Feature status events</h4>
 * <ul>
 *  <li><code>option_voting_changed</code></li>
 *  <li><code>option_watching_changed</code></li>
 *  <li><code>option_unassigned_issues_changed</code></li>
 *  <li><code>option_subtasks_changed</code></li>
 *  <li><code>option_attachments_changed</code></li>
 *  <li><code>option_issuelinks_changed</code></li>
 *  <li><code>option_timetracking_changed</code></li>
 * </ul>
 *
 * <h4>Confluence Webhook events</h4>
 *
 * <ul>
 *  <li><code>attachment_created</code></li>
 *  <li><code>attachment_removed</code></li>
 *  <li><code>attachment_updated</code></li>
 *  <li><code>attachment_viewed</code></li>
 *  <li><code>blog_created</code></li>
 *  <li><code>blog_removed</code></li>
 *  <li><code>blog_restored</code></li>
 *  <li><code>blog_trashed</code></li>
 *  <li><code>blog_updated</code></li>
 *  <li><code>blog_viewed</code></li>
 *  <li><code>blueprint_page_created</code></li>
 *  <li><code>cache_statistics_changed</code></li>
 *  <li><code>comment_created</code></li>
 *  <li><code>comment_removed</code></li>
 *  <li><code>comment_updated</code></li>
 *  <li><code>connect_addon_disabled</code></li>
 *  <li><code>connect_addon_enabled</code></li>
 *  <li><code>content_permissions_updated</code></li>
 *  <li><code>group_created</code></li>
 *  <li><code>group_removed</code></li>
 *  <li><code>label_added</code></li>
 *  <li><code>label_created</code></li>
 *  <li><code>label_deleted</code></li>
 *  <li><code>label_removed</code></li>
 *  <li><code>login</code></li>
 *  <li><code>login_failed</code></li>
 *  <li><code>logout</code></li>
 *  <li><code>page_children_reordered</code></li>
 *  <li><code>page_created</code></li>
 *  <li><code>page_moved</code></li>
 *  <li><code>page_removed</code></li>
 *  <li><code>page_restored</code></li>
 *  <li><code>page_trashed</code></li>
 *  <li><code>page_updated</code></li>
 *  <li><code>page_viewed</code></li>
 *  <li><code>plugin_enabled</code></li>
 *  <li><code>plugins_upgraded</code></li>
 *  <li><code>search_performed</code></li>
 *  <li><code>server_upgraded</code></li>
 *  <li><code>space_created</code></li>
 *  <li><code>space_logo_updated</code></li>
 *  <li><code>space_permissions_updated</code></li>
 *  <li><code>space_removed</code></li>
 *  <li><code>space_updated</code></li>
 *  <li><code>user_created</code></li>
 *  <li><code>user_deactivated</code></li>
 *  <li><code>user_followed</code></li>
 *  <li><code>user_reactivated</code></li>
 *  <li><code>user_removed</code></li>
 * </ul>
 *
 * <h4>Example Request</h4>
 *
 * <pre><code>
 * POST /jira-issue_created?user_id=admin&amp;user_key=admin HTTP/1.1
 * Authorization: JWT ...
 * Atlassian-Connect-Version: x.x
 * Content-Type: application/json
 * {
 *   timestamp: 1426661049725,
 *   webhookEvent: 'jira:issue_created',
 *   ...
 * }
 * </code></pre>
 *
 * <h3>Inspecting webhook contents</h3>
 *
 * Each type of webhook event includes information specific to that event in the body content of the POST message. The
 * add-on resource that listens for webhook posts should receive and process the content as appropriate for the add-on.
 * To understand what type of content each webhook generates, you can use the webhook inspector tool.
 *
 * The <a href="https://bitbucket.org/atlassianlabs/webhook-inspector">Webhook Inspector</a> is a
 * <a href="https://bitbucket.org/atlassian/atlassian-connect-express">atlassian-connect-express</a> Connect add-on
 * that you can install in your development environment to inspect the content of event messages. The Webhook Inspector
 * subscribes and generates each webhook event type available on the running instance of the Atlassian application,
 * and prints the body posted by the instance to the console screen.
 *
 * <h3>References</h3>
 * <ul>
 *  <li><a href="https://developer.atlassian.com/jiradev/jira-architecture/webhooks#Webhooks-Whatwilltheformatofthewebhookcallbackmessagebe%3F">JIRA Webhooks: What will the format of the webhook callback message be?</a></li>
 *  <li><a href="https://developer.atlassian.com/jiradev/jira-architecture/webhooks#Webhooks-SampleWebhookPOST">JIRA Webhooks: Sample Webhook POST</a></li>
 * </ul>
 *
 * <h3>Tutorials</h3>
 * <ul>
 *  <li><a href="https://developer.atlassian.com/confdev/tutorials/writing-a-multi-page-blueprint-using-atlassian-connect">Confluence Webhooks: Writing a Multi-page Blueprint using Atlassian Connect</a></li>
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
