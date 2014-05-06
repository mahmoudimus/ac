package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.WebHookModuleBeanBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A webhook is a standard mechanism for implementing HTTP callbacks. Atlassian OnDemand applications can execute
 * webhooks that your add-ons can use to be notified of certain events that happen in JIRA or Confluence.
 *
 * Just to give you an idea of how you can use them in add-ons, here are a few sample webhook events:
 *
 * * When an add-on is enabled or disabled
 * * When an issue is created or closed in JIRA
 * * When a page is created or updated in Confluence
 *
 *
 *### Handling the webhook event
 *
 *To receive webhook events, your add-on needs to include the webhook module declaration in its JSON descriptor. The
 * declaration indicates the relative URL of the local resource at which it will receive the notification. In other
 * words, the Atlassian application will send an HTTP POST to this resource in response to an application event. The
 * add-on code that handles the POST should process any information passed in the body of the message, as appropriate.
 * Each webhook POST sent to the add-on will also include the authentication headers that allow the add-on to validate
 * the authenticity of that request.
 *
 *### Webhook event types
 *
 * Below is a list of all available webhook events.
 *
 *#### Jira Webhook events
 *
 * * `connect_addon_disabled`
 * * `connect_addon_enabled`
 * * `jira-webhook-post-function`
 * * `jira:issue_created`
 * * `jira:issue_deleted`
 * * `jira:issue_updated`
 * * `jira:worklog_updated`
 * * `plugin_enabled`
 * * `plugins_upgraded`
 * * `remote_issue_link_aggregate_cleared_event`
 * * `remote_plugin_disabled`
 * * `remote_plugin_enabled`
 * * `remote_plugin_installed`
 * * `remote_workflow_post_function`
 * * `server_upgraded`
 *
 *#### Confluence Webhook events
 *
 * * `attachment_created`
 * * `attachment_removed`
 * * `attachment_updated`
 * * `attachment_viewed`
 * * `blog_created`
 * * `blog_removed`
 * * `blog_restored`
 * * `blog_trashed`
 * * `blog_updated`
 * * `blog_viewed`
 * * `cache_statistics_changed`
 * * `comment_created`
 * * `comment_removed`
 * * `comment_updated`
 * * `connect_addon_disabled`
 * * `connect_addon_enabled`
 * * `group_created`
 * * `group_removed`
 * * `label_added`
 * * `label_created`
 * * `label_deleted`
 * * `label_removed`
 * * `login`
 * * `login_failed`
 * * `logout`
 * * `page_children_reordered`
 * * `page_created`
 * * `page_moved`
 * * `page_removed`
 * * `page_restored`
 * * `page_trashed`
 * * `page_updated`
 * * `page_viewed`
 * * `plugin_enabled`
 * * `plugins_upgraded`
 * * `remote_plugin_disabled`
 * * `remote_plugin_enabled`
 * * `remote_plugin_installed`
 * * `search_performed`
 * * `server_upgraded`
 * * `space_created`
 * * `space_logo_updated`
 * * `space_permissions_updated`
 * * `space_removed`
 * * `space_updated`
 * * `status_cleared`
 * * `status_created`
 * * `status_removed`
 * * `user_created`
 * * `user_deactivated`
 * * `user_followed`
 * * `user_reactivated`
 * * `user_removed`
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
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#WEBHOOK_EXAMPLE}
 * @schemaTitle Webhooks
 * @since 1.0
 */
@SchemaDefinition("webhook")
public class WebHookModuleBean extends BeanWithParams
{
    /**
     * Specifies the named event you would like to listen to (e.g., "enabled", "jira:issue_created", etc.)
     */
    @Required
    private String event;
    /**
     * Specifies your add-on's POST webhook handler URL. This property has to be a relative URL.
     */
    @Required
    @StringSchemaAttributes (format = "uri")
    private String url;

    public WebHookModuleBean(WebHookModuleBeanBuilder builder)
    {
        super(builder);

        if (null == event)
        {
            this.event = "";
        }

        if (null == url)
        {
            this.url = "";
        }

    }

    protected WebHookModuleBean()
    {
        this.event = "";
        this.url = "";
    }

    public String getEvent()
    {
        return event;
    }

    public String getUrl()
    {
        return url;
    }

    public static WebHookModuleBeanBuilder newWebHookBean()
    {
        return new WebHookModuleBeanBuilder();
    }

    public static WebHookModuleBeanBuilder newWebHookBean(WebHookModuleBean defaultBean)
    {
        return new WebHookModuleBeanBuilder(defaultBean);
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof WebHookModuleBean && super.equals(otherObj)))
        {
            return false;
        }

        WebHookModuleBean other = (WebHookModuleBean) otherObj;

        return new EqualsBuilder()
                .append(url, other.url)
                .append(event, other.event)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(13, 61)
                .appendSuper(super.hashCode())
                .append(url)
                .append(event)
                .build();
    }
}
