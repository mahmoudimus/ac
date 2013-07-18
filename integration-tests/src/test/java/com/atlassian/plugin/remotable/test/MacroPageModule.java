package com.atlassian.plugin.remotable.test;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import org.dom4j.Element;

import javax.servlet.http.HttpServlet;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;

public class MacroPageModule extends AbstractModule
{
    private final String key;
    private Option<String> name = none();
    private Option<String> path = none();
    private Option<String> outputType = none();
    private Option<String> bodyType = none();
    private Option<HttpServlet> resource = none();

    public MacroPageModule(String key)
    {
        this.key = checkNotNull(key);
    }

    public static MacroPageModule key(String key)
    {
        return new MacroPageModule(key);
    }

    public MacroPageModule name(String name)
    {
        this.name = option(name);
        return this;
    }


    public MacroPageModule path(String path)
    {
        this.path = option(path);
        return this;
    }

    public MacroPageModule outputType(String outputType)
    {
        this.outputType = option(outputType);
        return this;
    }

    public MacroPageModule bodyType(String bodyType)
    {
        this.bodyType = option(bodyType);
        return this;
    }

    public MacroPageModule resource(HttpServlet resource)
    {
        this.resource = option(resource);
        return this;
    }

    @Override
    public void update(Element el)
    {
        final Element macroPage = el.addElement("macro-page").addAttribute("key", key);
        addAttribute(macroPage, "name", name);
        addAttribute(macroPage, "url", path);
        addAttribute(macroPage, "output-type", outputType);
        addAttribute(macroPage, "body-type", bodyType);
    }

    @Override
    public Option<Pair<String, HttpServlet>> getResource()
    {
        if (path.isDefined() && resource.isDefined())
        {
            return some(Pair.pair(path.get(), resource.get()));
        }
        else
        {
            return none();
        }
    }
}
