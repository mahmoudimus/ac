package com.atlassian.plugin.remotable.test;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.dom4j.Element;

import javax.servlet.http.HttpServlet;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;

public class GeneralPageModule extends AbstractModule
{
    private final String key;
    private Option<String> path = none();
    private Option<String> name = none();
    private Option<String> linkName = none();
    private Option<String> iconUrl = none();
    private Option<String> height = none();
    private Option<String> width = none();
    private Option<HttpServlet> servlet = none();
    private Iterable<Condition> conditions = ImmutableList.of();

    private GeneralPageModule(String key)
    {
        this.key = checkNotNull(key);
    }

    public static GeneralPageModule key(String key)
    {
        return new GeneralPageModule(key);
    }

    @Override
    public void update(Element el)
    {
        final Element generalPage = el
                .addElement("general-page")
                .addAttribute("key", key);

        addAttribute(generalPage, "url", path);
        addAttribute(generalPage, "name", name);
        addAttribute(generalPage, "link-name", linkName);
        addAttribute(generalPage, "icon-url", iconUrl);
        addAttribute(generalPage, "width", width);
        addAttribute(generalPage, "height", height);
        if (!Iterables.isEmpty(conditions))
        {
            final Element conditionsEl = generalPage.addElement("conditions");
            for (Condition condition : conditions)
            {
                condition.update(conditionsEl);
            }
        }
    }

    @Override
    public Option<Pair<String, HttpServlet>> getResource()
    {
        if (path.isDefined() && servlet.isDefined())
        {
            return some(Pair.pair(path.get(), servlet.get()));
        }
        else
        {
            return none();
        }
    }

    public GeneralPageModule name(String name)
    {
        this.name = option(name);
        return this;
    }

    public GeneralPageModule path(String path)
    {
        this.path = option(path);
        return this;
    }

    public String path()
    {
        return path.getOrNull();
    }

    public GeneralPageModule linkName(String linkName)
    {
        this.linkName = option(linkName);
        return this;
    }

    public GeneralPageModule iconUrl(String icon)
    {
        this.iconUrl = option(icon);
        return this;
    }

    public GeneralPageModule height(String h)
    {
        this.height = option(h);
        return this;
    }

    public GeneralPageModule width(String w)
    {
        this.width = option(w);
        return this;
    }

    public GeneralPageModule conditions(Condition... conditions)
    {
        this.conditions = ImmutableList.copyOf(conditions);
        return this;
    }

    public GeneralPageModule resource(HttpServlet servlet)
    {
        this.servlet = option(servlet);
        return this;
    }

    public Iterable<Condition> conditions()
    {
        return conditions;
    }
}
