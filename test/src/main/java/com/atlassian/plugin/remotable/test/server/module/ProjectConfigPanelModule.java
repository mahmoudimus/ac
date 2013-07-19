package com.atlassian.plugin.remotable.test.server.module;

import com.atlassian.fugue.Option;
import org.dom4j.Element;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;

public final class ProjectConfigPanelModule extends MainModuleWithResource<ProjectConfigPanelModule>
{
    private Option<String> location = none();

    private ProjectConfigPanelModule(String key)
    {
        super("project-config-panel", key);
    }

    public static ProjectConfigPanelModule key(String key)
    {
        return new ProjectConfigPanelModule(key);
    }

    public ProjectConfigPanelModule location(String location)
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
