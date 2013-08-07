package com.atlassian.plugin.connect.test.server.module;

import com.atlassian.fugue.Option;

import org.dom4j.Element;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;

public final class ProjectConfigTabModule extends MainModuleWithResource<ProjectConfigTabModule>
{
    private Option<String> weight = none();
    private Option<String> location = none();

    private ProjectConfigTabModule(String key)
    {
        super("project-config-tab", key);
    }

    public static ProjectConfigTabModule key(String key)
    {
        return new ProjectConfigTabModule(key);
    }

    public ProjectConfigTabModule location(String location)
    {
        this.location = option(location);
        return this;
    }

    public ProjectConfigTabModule weight(String weight)
    {
        this.weight = option(weight);
        return this;
    }

    @Override
    protected void addYetOthersToElement(Element el)
    {
        addAttribute(el, "weight", weight);
        addAttribute(el, "location", location);
    }
}
