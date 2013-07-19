package com.atlassian.plugin.remotable.test.server.module;

import com.atlassian.fugue.Option;
import org.dom4j.Element;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;

public final class MacroEditor extends AbstractModuleWithResource<MacroEditor>
{
    private Option<String> height = none();
    private Option<String> width = none();

    private MacroEditor(String path)
    {
        super("macro-editor");
        this.path = some(checkNotNull(path));
    }

    public static MacroEditor at(String path)
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

    @Override
    protected void addToElement(Element el)
    {
        addAttribute(el, "url", path);
        addAttribute(el, "width", width);
        addAttribute(el, "height", height);
    }
}
