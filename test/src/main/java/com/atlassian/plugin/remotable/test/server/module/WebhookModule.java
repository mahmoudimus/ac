package com.atlassian.plugin.remotable.test.server.module;

import com.atlassian.fugue.Option;
import org.dom4j.Element;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;

public final class WebhookModule extends MainModuleWithResource<WebhookModule>
{
    private Option<String> event = none();

    private WebhookModule(String key)
    {
        super("webhook", key);
    }

    public static WebhookModule key(String key)
    {
        return new WebhookModule(key);
    }

    public WebhookModule event(String event)
    {
        this.event = option(event);
        return this;
    }

    @Override
    protected void addYetOthersToElement(Element el)
    {
        addAttribute(el, "event", event);
    }
}
