package com.atlassian.plugin.remotable.test.server.module;

import com.atlassian.fugue.Option;
import org.dom4j.Element;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;

public final class ContextParameter extends AbstractModule
{
    private final String name;
    private Option<String> type = none();

    private ContextParameter(String name)
    {
        super("context-parameter");
        this.name = checkNotNull(name);
    }

    public static ContextParameter name(String name)
    {
        return new ContextParameter(name);
    }

    public ContextParameter query()
    {
        return type("query");
    }

    public ContextParameter header()
    {
        return type("header");
    }

    private ContextParameter type(String type)
    {
        this.type = option(type);
        return this;
    }

    @Override
    protected void addToElement(Element el)
    {
        addAttribute(el, "name", some(name));
        addAttribute(el, "type", type);
    }
}
