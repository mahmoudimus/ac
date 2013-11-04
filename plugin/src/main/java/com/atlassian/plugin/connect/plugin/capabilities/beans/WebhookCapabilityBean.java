package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.WebhookCapabilityBeanBuilder;

/**
 * Webhook registration for an internal event
 *
 * Sample capability:
 *
 *  {
 *      "webhooks": [
 *          {
 *              "event": "issue_created",
 *              "url": "/issue-created"
 *          },
 *          ...
 *          {
 *              "event": "issue_updated",
 *              "url": "/issue-updated"
 *          }
 *      ]
 *  }
 *
 */
public class WebhookCapabilityBean extends BeanWithParams
{
    private String event;
    private String url;

    public WebhookCapabilityBean(WebhookCapabilityBeanBuilder builder) {
        super(builder);

        if(null == event)
        {
            this.event = "";
        }

        if(null == url)
        {
            this.url = "";
        }

    }

    protected WebhookCapabilityBean() {
        this.event = "";
        this.url = "";
    }

    public String getEvent() {
        return event;
    }

    public String getUrl() {
        return url;
    }

    public static WebhookCapabilityBeanBuilder newWebhookBean()
    {
        return new WebhookCapabilityBeanBuilder();
    }

    public static WebhookCapabilityBeanBuilder newWebhookBean(WebhookCapabilityBean defaultBean)
    {
        return new WebhookCapabilityBeanBuilder(defaultBean);
    }

}
