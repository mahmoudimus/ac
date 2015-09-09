package com.atlassian.plugin.connect.test.server.module;

import com.atlassian.fugue.Option;

import org.dom4j.Element;

import static com.atlassian.fugue.Option.*;
import static com.google.common.base.Preconditions.checkNotNull;

abstract class MainModuleWithResource<T extends MainModuleWithResource> extends AbstractModuleWithResource<T>
{
    private final String key;
    private Option<String> name = none();

    protected MainModuleWithResource(String name, String key)
    {
        super(name);
        this.key = checkNotNull(key);
    }

    public T name(String name)
    {
        this.name = option(name);
        return cast();
    }

    @Override
    protected final void addOthersToElement(Element el)
    {
        addAttribute(el, "key", some(key));
        addAttribute(el, "name", name);
        addYetOthersToElement(el);
    }

    protected void addYetOthersToElement(Element el)
    {
    }
}
