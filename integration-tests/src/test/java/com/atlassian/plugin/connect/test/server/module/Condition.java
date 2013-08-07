package com.atlassian.plugin.connect.test.server.module;

import com.atlassian.fugue.Option;

import org.dom4j.Element;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;

public final class Condition extends AbstractModuleWithResource<Condition>
{
    private Option<String> name = none();

    private Condition()
    {
        super("condition");
    }

    public static Condition name(String name)
    {
        Condition condition = new Condition();
        condition.name = option(name);
        return condition;
    }

    public static Condition at(String path)
    {
        Condition condition = new Condition();
        condition.path = option(path);
        return condition;
    }

    @Override
    protected void addOthersToElement(Element el)
    {
        if (name.isDefined())
        {
            addAttribute(el, "name", name);
        }
        else if (path.isDefined())
        {
            addAttribute(el, "url", path);
        }
    }
}
