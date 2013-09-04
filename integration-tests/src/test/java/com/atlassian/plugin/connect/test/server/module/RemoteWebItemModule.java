package com.atlassian.plugin.connect.test.server.module;

import com.atlassian.fugue.Option;
import org.dom4j.Element;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.some;

public class RemoteWebItemModule extends MainModuleWithResource<RemoteWebItemModule>
{
    private Option<String> section = none();
    private Option<Integer> weight = none();
    private Option<Link> link = none();
    private Option<Icon> icon = none();

    protected RemoteWebItemModule(String key)
    {
        super("remote-web-item", key);
    }

    public static RemoteWebItemModule key(String key)
    {
        return new RemoteWebItemModule(key);
    }

    public RemoteWebItemModule section(String section)
    {
        this.section = some(section);
        return this;
    }

    public RemoteWebItemModule weight(Integer weight)
    {
        this.weight = some(weight);
        return this;
    }

    public RemoteWebItemModule link(Link link)
    {
        this.link = some(link);
        return this;
    }

    public RemoteWebItemModule icon(Icon icon)
    {
        this.icon = some(icon);
        return this;
    }

    @Override
    protected void addYetOthersToElement(final Element el)
    {
        if (icon.isDefined())
        {
            icon.get().addToElement(el);
        }
        link.get().addToElement(el);
        el.addAttribute("section", section.get());
        if (weight.isDefined())
        {
            el.addAttribute("weight", String.valueOf(weight.get()));
        }
    }

    public static class Link
    {
        private Option<String> path;
        private Option<Boolean> absolute;

        public Link(String link, boolean absolute)
        {
            this.path = some(link);
            this.absolute = some(absolute);
        }

        public static Link link(String link, boolean absolute)
        {
            return new Link(link, absolute);
        }

        public void addToElement(Element el)
        {
            el.addElement("link")
                    .addAttribute("absolute", String.valueOf(absolute.isDefined() ? absolute.get() : false))
                    .setText(path.get());
        }
    }

    public static class Icon
    {
        private Option<Integer> width = none();
        private Option<Integer> height = none();
        private Option<Link> link = none();

        public Icon(final int width, final int height, final String link)
        {
            this.width = some(width);
            this.height = some(height);
            this.link = some(Link.link(link, false));
        }

        public static Icon icon(int width, int height, String link)
        {
            return new Icon(width, height, link);
        }

        public void addToElement(Element el)
        {
            el.addElement("icon").addAttribute("width", String.valueOf(width.get()))
                    .addAttribute("height", String.valueOf(height.get()));
            link.get().addToElement(el.element("icon"));
        }
    }

}
