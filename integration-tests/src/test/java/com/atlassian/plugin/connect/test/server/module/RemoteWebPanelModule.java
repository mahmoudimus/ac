package com.atlassian.plugin.connect.test.server.module;

import com.atlassian.fugue.Option;

import org.dom4j.Element;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;

public final class RemoteWebPanelModule extends MainModuleWithResource<RemoteWebPanelModule>
{
    private Option<String> location = none();

    private RemoteWebPanelModule(String key)
    {
        super("remote-web-panel", key);
    }

    public static RemoteWebPanelModule key(String key)
    {
        return new RemoteWebPanelModule(key);
    }

    public RemoteWebPanelModule location(String location)
    {
        this.location = option(location);
        return this;
    }

    @Override
    protected void addYetOthersToElement(Element el)
    {
        addAttribute(el, "location", location);
    }
}
