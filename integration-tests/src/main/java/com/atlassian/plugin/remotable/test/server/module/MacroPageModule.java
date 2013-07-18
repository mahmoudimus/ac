package com.atlassian.plugin.remotable.test.server.module;

import com.atlassian.fugue.Option;
import org.dom4j.Element;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;

public final class MacroPageModule extends AbstractModuleWithResource<MacroPageModule>
{
    private final String key;
    private Option<String> name = none();
    private Option<String> outputType = none();
    private Option<String> bodyType = none();

    public MacroPageModule(String key)
    {
        super("macro-page");
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

    @Override
    protected void addToElement(Element el)
    {
        addAttribute(el, "key", some(key));
        addAttribute(el, "name", name);
        addAttribute(el, "url", path);
        addAttribute(el, "output-type", outputType);
        addAttribute(el, "body-type", bodyType);
    }
}
