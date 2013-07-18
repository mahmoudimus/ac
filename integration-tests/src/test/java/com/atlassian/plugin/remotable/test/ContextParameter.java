package com.atlassian.plugin.remotable.test;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import org.dom4j.Element;

import javax.servlet.http.HttpServlet;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static com.google.common.base.Preconditions.checkNotNull;

public class ContextParameter extends AbstractModule
{
    private final String name;
    private Option<String> type = none();

    private ContextParameter(String name)
    {
        this.name = checkNotNull(name);
    }

    public static ContextParameter name(String name)
    {
        return new ContextParameter(name);
    }

    public ContextParameter type(String type)
    {
        this.type = option(type);
        return this;
    }

    @Override
    public void update(Element el)
    {
        final Element contextParam = el.addElement("context-parameter").addAttribute("name", name);
        addAttribute(contextParam, "type", type);
    }

    @Override
    public Option<Pair<String, HttpServlet>> getResource()
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
