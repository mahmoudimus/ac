package com.atlassian.plugin.connect.test.server.module;

import org.dom4j.Element;

import static com.atlassian.fugue.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;

public final class MacroCategory extends AbstractModule
{
    private final String name;

    private MacroCategory(String name)
    {
        super("category");
        this.name = checkNotNull(name);
    }

    public static MacroCategory name(String name)
    {
        return new MacroCategory(name);
    }

    @Override
    protected void addToElement(Element el)
    {
        addAttribute(el, "name", some(name));
    }
}
