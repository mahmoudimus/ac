package com.atlassian.plugin.remotable.test;

import com.atlassian.fugue.Option;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.dom4j.Element;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractModule implements Module
{
    protected Element addAttribute(Element el, String name, Option<String> value)
    {
        return value.map(new AddAttribute(el, name)).getOrElse(el);
    }

    protected void addElement(final Element el, Option<? extends Module> module)
    {
        module.map(new AddModuleFunction(el));
    }

    protected void addElements(Element el, Iterable<? extends Module> modules, String name)
    {
        if (!Iterables.isEmpty(modules))
        {
            final Element collectionEl = el.addElement(name);
            for (Module m : modules)
            {
                m.update(collectionEl);
            }
        }
    }

    private static final class AddAttribute implements Function<String, Element>
    {
        private final Element el;
        private final String name;

        public AddAttribute(Element el, String name)
        {
            this.el = checkNotNull(el);
            this.name = checkNotNull(name);
        }

        @Override
        public Element apply(String path)
        {

            return el.addAttribute(name, path);
        }
    }

    private static class AddModuleFunction implements Function<Module, Void>
    {
        private final Element el;

        public AddModuleFunction(Element el)
        {
            this.el = el;
        }

        @Override
        public Void apply(Module mc)
        {
            mc.update(el);
            return null;
        }
    }
}
