package com.atlassian.plugin.remotable.test;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.google.common.collect.ImmutableList;
import org.dom4j.Element;

import javax.servlet.http.HttpServlet;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
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
    public void update(Element el)
    {
        final Element parameter = el.addElement("parameter").addAttribute("name", name);
        addAttribute(parameter, "title", title);
        addAttribute(parameter, "type", type);
        addAttribute(parameter, "required", required);

        for (String value : values)
        {
            parameter.addElement("value").addAttribute("name", value);
        }
    }

    @Override
    public Option<Pair<String, HttpServlet>> getResource()
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
