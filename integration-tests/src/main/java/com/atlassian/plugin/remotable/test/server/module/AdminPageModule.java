package com.atlassian.plugin.remotable.test.server.module;

import com.atlassian.fugue.Option;
import org.dom4j.Element;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;

public final class AdminPageModule extends AbstractModuleWithResource<AdminPageModule>
{
    private final String key;
    private Option<String> name = none();
    private Option<String> section = none();

    private AdminPageModule(String key)
    {
        super("admin-page");
        this.key = checkNotNull(key);
    }

    public static AdminPageModule key(String key)
    {
        return new AdminPageModule(key);
    }

    public AdminPageModule name(String name)
    {
        this.name = option(name);
        return this;
    }

    public AdminPageModule section(String section)
    {
        this.section = option(section);
        return this;
    }

    @Override
    protected void addToElement(Element el)
    {
        addAttribute(el, "key", some(key));
        addAttribute(el, "name", name);
        addAttribute(el, "url", path);
        addAttribute(el, "section", section);
    }
}
