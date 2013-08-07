package com.atlassian.plugin.connect.test.server.module;

import com.atlassian.fugue.Option;

import org.dom4j.Element;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;

public final class MacroPageModule extends MainModuleWithResource<MacroPageModule>
{
    private Option<String> outputType = none();
    private Option<String> bodyType = none();

    public MacroPageModule(String key)
    {
        super("macro-page", key);
    }

    public static MacroPageModule key(String key)
    {
        return new MacroPageModule(key);
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
    protected void addYetOthersToElement(Element el)
    {
        addAttribute(el, "output-type", outputType);
        addAttribute(el, "body-type", bodyType);
    }
}
