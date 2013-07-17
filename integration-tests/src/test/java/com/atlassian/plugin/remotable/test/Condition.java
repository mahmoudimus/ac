package com.atlassian.plugin.remotable.test;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import org.dom4j.Element;

import javax.servlet.http.HttpServlet;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Option.some;
import static com.atlassian.fugue.Pair.pair;

public final class Condition implements Module
{
    private Option<String> name = none();
    private Option<String> path = none();
    private Option<HttpServlet> servlet = none();

    private Condition()
    {
    }

    public static Condition name(String name)
    {
        Condition condition = new Condition();
        condition.name = option(name);
        return condition;
    }

    public static Condition path(String path)
    {
        Condition condition = new Condition();
        condition.path = option(path);
        return condition;
    }

    public Condition resource(HttpServlet servlet)
    {
        this.servlet = option(servlet);
        return this;
    }

    @Override
    public void update(Element el)
    {
        final Element condition = el.addElement("condition");
        if (name.isDefined())
        {
            condition.addAttribute("name", name.get());
        }
        else if (path.isDefined())
        {
            condition.addAttribute("url", path.get());
        }
    }

    @Override
    public Option<Pair<String, HttpServlet>> getResource()
    {
        if (path.isDefined() && servlet.isDefined())
        {
            return some(pair(path.get(), servlet.get()));
        }
        else
        {
            return none();
        }
    }
}
