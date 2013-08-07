package com.atlassian.plugin.connect.test.server.module;

import com.atlassian.fugue.Option;

import org.dom4j.Element;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;

public final class AdminPageModule extends MainModuleWithResource<AdminPageModule>
{
    private Option<String> section = none();

    private AdminPageModule(String key)
    {
        super("admin-page", key);
    }

    public static AdminPageModule key(String key)
    {
        return new AdminPageModule(key);
    }

    public AdminPageModule section(String section)
    {
        this.section = option(section);
        return this;
    }

    @Override
    protected void addYetOthersToElement(Element el)
    {
        addAttribute(el, "section", section);
    }
}
