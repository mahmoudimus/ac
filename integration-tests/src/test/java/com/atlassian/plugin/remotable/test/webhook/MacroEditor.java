package com.atlassian.plugin.remotable.test.webhook;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.atlassian.plugin.remotable.test.AbstractModule;
import org.dom4j.Element;

import javax.servlet.http.HttpServlet;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Option.some;
import static com.atlassian.fugue.Pair.pair;
import static com.google.common.base.Preconditions.checkNotNull;

public class MacroEditor extends AbstractModule
{
    private final String path;
    private Option<String> height = none();
    private Option<String> width = none();
    private Option<HttpServlet> servlet = none();

    private MacroEditor(String path)
    {
        this.path = checkNotNull(path);
    }

    public static MacroEditor path(String path)
    {
        return new MacroEditor(path);
    }

    public MacroEditor height(String height)
    {
        this.height = option(height);
        return this;
    }


    public MacroEditor width(String width)
    {
        this.width = option(width);
        return this;
    }

    public MacroEditor resource(HttpServlet servlet)
    {
        this.servlet = option(servlet);
        return this;
    }

    @Override
    public void update(Element el)
    {
        final Element macroEditor = el.addElement("macro-editor").addAttribute("url", path);
        addAttribute(macroEditor, "width", width);
        addAttribute(macroEditor, "height", height);
    }

    @Override
    public Option<Pair<String, HttpServlet>> getResource()
    {
        if (servlet.isDefined())
        {
            return some(pair(path, servlet.get()));
        }
        else
        {
            return none();
        }
    }
}
