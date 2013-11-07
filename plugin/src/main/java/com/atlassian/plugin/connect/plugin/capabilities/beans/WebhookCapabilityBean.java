package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.WebHookCapabilityBeanBuilder;

/**
 * A webhook is a standard mechanism for implementing HTTP callbacks. Atlassian OnDemand applications can execute
 * webhooks that your add-ons can use to be notified of certain events that happen in JIRA or Confluence.
 * <p/>
 * Just to give you an idea of how you can use them in add-ons, here are a few sample webhook events:
 * <p/>
 * <ul>
 * <li>When an add-on is enabled or disabled</li>
 * <li>When an issue is created or closed in JIRA</li>
 * <li>When a page is created or updated in Confluence</li>
 * </ul>
 * <p/>
 * While some webhooks are specific to the Atlassian application, JIRA or Confluence, others are common to all
 * applications. This type includes, for example, the webhooks that generate notifications when the add-on is enabled.
 * <p/>
 * Most add-ons should implement the remote_plugin_enabled webhook. The Atlassian application uses this event to supply
 * its public key to the add-on. The Authenticating with OAuth page shows an example of how to use this webhook.
 * <p/>
 * <h3>Handling the webhook event</h3>
 * To receive webhook events, your add-on needs to include the webhook module declaration in its JSON descriptor. The
 * declaration indicates the relative URL of the local resource at which it will receive the notification. In other
 * words, the Atlassian application will send an HTTP POST to this resource in response to an application event. The
 * add-on code that handles the POST should process any information passed in the body of the message, as appropriate.
 * Each webhook POST sent to the add-on will also include the authentication headers that allow the add-on to
 * validate the authenticity of that request.
 * <p/>
 * <h4>Sample webhooks JSON block</h4>
 * <pre>
 * {@code
 * {
 *     "webhooks": [
 *         {
 *             "event": "jira:issue_created",
 *             "url": "/issue-created"
 *         },
 *         ...
 *         {
 *             "event": "jira:issue_updated",
 *             "url": "/issue-updated"
 *         }
 *     ]
 * }
 * }
 * </pre>
 * <h3>Webhook event types</h3>
 * To find out what webhooks are available for each application, the best place to look is in the Interactive
 * Descriptor Guide (<a href="https://developer.atlassian.com/connect/api/jira">JIRA</a> or
 * <a href="https://developer.atlassian.com/connect/api/confluence">Confluence</a>).
 * <p/>
 * <h3>Inspecting webhook contents</h3>
 * Each type of webhook event includes information specific to that event in the body content of the POST message. The
 * add-on resource that listens for webhook posts should receive and process the content as appropriate for the add-on.
 * To understand what type of content each webhook generates, you can use the webhook inspector tool.
 * <p/>
 * The <a href="https://bitbucket.org/atlassianlabs/webhook-inspector">Webhook Inspector</a> is a
 * <a href="https://bitbucket.org/atlassian/atlassian-connect-express">atlassian-connect-express</a> Connect add-on
 * that you can install in your development environment to inspect the content of event messages. The Webhook Inspector
 * subscribes and generates each webhook event type available on the running instance of the Atlassian application,
 * and prints the body posted by the instance to the console screen.
 *
 * @since 1.0
 */
public class WebHookCapabilityBean extends BeanWithParams
{
    /**
     * Specifies the named event you would like to listen to (e.g., "enabled", "jira:issue_created", etc.)
     */
    private String event;
    /**
     * Specifies your add-on's POST webhook handler URL. This property has to be a relative URL.
     */
    private String url;

    public WebHookCapabilityBean(WebHookCapabilityBeanBuilder builder)
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

    protected WebHookCapabilityBean()
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

    public static WebHookCapabilityBeanBuilder newWebHookBean()
    {
        return new WebHookCapabilityBeanBuilder();
    }

    public static WebHookCapabilityBeanBuilder newWebhookBean(WebHookCapabilityBean defaultBean)
    {
        return new WebHookCapabilityBeanBuilder(defaultBean);
    }

}
