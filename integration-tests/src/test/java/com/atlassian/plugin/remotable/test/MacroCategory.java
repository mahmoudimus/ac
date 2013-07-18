package com.atlassian.plugin.remotable.test;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import org.dom4j.Element;

import javax.servlet.http.HttpServlet;

import static com.google.common.base.Preconditions.checkNotNull;

public class MacroCategory extends AbstractModule
{
    private final String name;

    private MacroCategory(String name)
    {
        this.name = checkNotNull(name);
    }

    public static MacroCategory name(String name)
    {
        return new MacroCategory(name);
    }

    @Override
    public void update(Element el)
    {
        el.addElement("category").addAttribute("name", name);
    }

    @Override
    public Option<Pair<String, HttpServlet>> getResource()
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
