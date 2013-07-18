package com.atlassian.plugin.remotable.test;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import org.dom4j.Element;

import javax.servlet.http.HttpServlet;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Pair.pair;
import static com.google.common.base.Preconditions.checkNotNull;

public class ImagePlaceHolder extends AbstractModule
{
    private final String path;
    private Option<HttpServlet> servlet = none();

    private ImagePlaceHolder(String path)
    {
        this.path = checkNotNull(path);
    }

    public static ImagePlaceHolder path(String path)
    {
        return new ImagePlaceHolder(path);
    }

    public ImagePlaceHolder resource(HttpServlet servlet)
    {
        this.servlet = option(servlet);
        return null;
    }

    @Override
    public void update(Element el)
    {
        el.addElement("image-placeholder").addAttribute("url", path);
    }

    @Override
    public Option<Pair<String, HttpServlet>> getResource()
    {
        if (servlet.isDefined())
        {
            return option(pair(path, servlet.get()));
        }
        else
        {
            return none();
        }
    }
}
