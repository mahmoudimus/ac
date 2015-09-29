package com.atlassian.plugin.connect.test.server.module;

import com.atlassian.fugue.Option;

import org.dom4j.Element;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;

public final class DialogPageModule extends MainModuleWithResource<DialogPageModule>
{
    private Option<String> section = none();

    private DialogPageModule(String key)
    {
        super("dialog-page", key);
    }

    public static DialogPageModule key(String key)
    {
        return new DialogPageModule(key);
    }

    public DialogPageModule section(String section)
    {
        this.section = option(section);
        return this;
    }

    @Override
    protected void addYetOthersToElement(Element el)
    {
        addAttribute(el, "section", section);
    }
}
