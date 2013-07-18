package com.atlassian.plugin.remotable.test.server.module;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.google.common.collect.ImmutableList;
import org.dom4j.Element;

import javax.servlet.http.HttpServlet;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;

public final class GeneralPageModule extends AbstractModuleWithResource<GeneralPageModule>
{
    private final String key;
    private Option<String> name = none();
    private Option<String> linkName = none();
    private Option<String> iconUrl = none();
    private Option<String> height = none();
    private Option<String> width = none();
    private Iterable<Condition> conditions = ImmutableList.of();

    private GeneralPageModule(String key)
    {
        super("general-page");
        this.key = checkNotNull(key);
    }

    public static GeneralPageModule key(String key)
    {
        return new GeneralPageModule(key);
    }

    public GeneralPageModule name(String name)
    {
        this.name = option(name);
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

    public Iterable<Condition> conditions()
    {
        return conditions;
    }

    @Override
    public void addToElement(Element el)
    {
        addAttribute(el, "key", some(key));
        addAttribute(el, "url", path);
        addAttribute(el, "name", name);
        addAttribute(el, "link-name", linkName);
        addAttribute(el, "icon-url", iconUrl);
        addAttribute(el, "width", width);
        addAttribute(el, "height", height);
        addElements(el, "conditions", conditions);
    }

    @Override
    public Iterable<Pair<String, HttpServlet>> getSubResources()
    {
        final ImmutableList.Builder<Pair<String, HttpServlet>> resources = ImmutableList.builder();
        for (Condition condition : conditions)
        {
            resources.addAll(condition.getResources());
        }
        return resources.build();
    }
}
