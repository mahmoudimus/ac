package com.atlassian.plugin.connect.test.server.module;

import com.atlassian.fugue.Option;

import com.google.common.collect.ImmutableList;

import org.dom4j.Element;

import static com.atlassian.fugue.Option.*;
import static com.google.common.base.Preconditions.checkNotNull;

public class MacroParameter extends AbstractModule
{
    private final String name;
    private Option<String> title = none();
    private Option<String> type = none();
    private Option<String> required = none();
    private Iterable<String> values = ImmutableList.of();

    private MacroParameter(String name)
    {
        super("parameter");
        this.name = checkNotNull(name);
    }

    public static MacroParameter name(String name)
    {
        return new MacroParameter(name);
    }

    public MacroParameter title(String title)
    {
        this.title = option(title);
        return this;
    }

    public MacroParameter type(String type)
    {
        this.type = option(type);
        return this;
    }


    public MacroParameter required(String required)
    {
        this.required = option(required);
        return this;
    }

    public MacroParameter values(String... values)
    {
        this.values = ImmutableList.copyOf(values);
        return this;
    }

    @Override
    protected void addToElement(Element el)
    {
        addAttribute(el, "name", some(name));
        addAttribute(el, "title", title);
        addAttribute(el, "type", type);
        addAttribute(el, "required", required);
        for (String value : values)
        {
            el.addElement("value").addAttribute("name", value);
        }
    }
}
