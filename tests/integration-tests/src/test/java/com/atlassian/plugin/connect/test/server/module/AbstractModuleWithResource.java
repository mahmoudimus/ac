package com.atlassian.plugin.connect.test.server.module;

import javax.servlet.http.HttpServlet;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;

import com.google.common.collect.ImmutableList;

import org.dom4j.Element;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Pair.pair;

abstract class AbstractModuleWithResource<T extends AbstractModuleWithResource> extends AbstractModule
{
    protected Option<String> path = none();
    protected Option<HttpServlet> servlet = none();

    protected AbstractModuleWithResource(String name)
    {
        super(name);
    }

    public final T path(String path)
    {
        this.path = option(path);
        return cast();
    }

    public final T resource(HttpServlet servlet)
    {
        this.servlet = option(servlet);
        return cast();
    }

    @SuppressWarnings("unchecked")
    protected final T cast()
    {
        return (T) this;
    }

    @Override
    protected final void addToElement(Element el)
    {
        addAttribute(el, "url", path);
        addOthersToElement(el);
    }

    protected void addOthersToElement(Element el)
    {
    }

    @Override
    public final Iterable<Pair<String, HttpServlet>> getResources()
    {
        final ImmutableList.Builder<Pair<String, HttpServlet>> resources = ImmutableList.builder();
        if (path.isDefined() && servlet.isDefined())
        {
            resources.add(pair(path.get(), servlet.get()));
        }
        resources.addAll(getSubResources());
        return resources.build();
    }

    protected Iterable<? extends Pair<String, HttpServlet>> getSubResources()
    {
        return ImmutableList.of();
    }
}
