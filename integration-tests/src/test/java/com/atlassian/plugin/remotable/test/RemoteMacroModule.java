package com.atlassian.plugin.remotable.test;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.atlassian.plugin.remotable.test.webhook.MacroEditor;
import com.google.common.collect.ImmutableList;
import org.dom4j.Element;

import javax.servlet.http.HttpServlet;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Option.some;
import static com.atlassian.fugue.Pair.pair;
import static com.google.common.base.Preconditions.checkNotNull;

public class RemoteMacroModule extends AbstractModule
{
    private final String key;
    private Option<String> name = none();
    private Option<String> title = none();
    private Option<String> path = none();
    private Option<String> iconUrl = none();
    private Option<String> outputType = none();
    private Option<String> bodyType = none();
    private Option<String> featured = none();
    private Option<MacroCategory> category = none();
    private Iterable<MacroParameter> parameters = ImmutableList.of();
    private Iterable<ContextParameter> contextParameters = ImmutableList.of();
    private Option<MacroEditor> editor = none();
    private Option<HttpServlet> servlet = none();
    private Option<ImagePlaceHolder> imagePlaceHolder = none();

    private RemoteMacroModule(String key)
    {
        this.key = checkNotNull(key);
    }

    public static RemoteMacroModule key(String key)
    {
        return new RemoteMacroModule(key);
    }


    public RemoteMacroModule name(String name)
    {
        this.name = option(name);
        return this;
    }

    public RemoteMacroModule title(String title)
    {
        this.title = option(title);
        return this;
    }

    public RemoteMacroModule path(String path)
    {
        this.path = option(path);
        return this;
    }

    public RemoteMacroModule iconUrl(String icon)
    {
        this.iconUrl = some(icon);
        return this;
    }

    public RemoteMacroModule outputType(String outputType)
    {
        this.outputType = option(outputType);
        return this;
    }

    public RemoteMacroModule bodyType(String bodyType)
    {
        this.bodyType = option(bodyType);
        return this;
    }

    public RemoteMacroModule featured(String featured)
    {
        this.featured = option(featured);
        return this;
    }

    public RemoteMacroModule category(MacroCategory category)
    {
        this.category = option(category);
        return this;
    }

    public RemoteMacroModule parameters(MacroParameter... parameters)
    {
        this.parameters = ImmutableList.copyOf(parameters);
        return this;
    }

    public RemoteMacroModule contextParameters(ContextParameter... contextParameters)
    {
        this.contextParameters = ImmutableList.copyOf(contextParameters);
        return this;
    }

    public RemoteMacroModule editor(MacroEditor editor)
    {
        this.editor = option(editor);
        return this;
    }

    public Option<MacroEditor> editor()
    {
        return editor;
    }

    public RemoteMacroModule resource(HttpServlet servlet)
    {
        this.servlet = option(servlet);
        return this;
    }

    public RemoteMacroModule imagePlaceHolder(ImagePlaceHolder img)
    {
        this.imagePlaceHolder = option(img);
        return this;
    }

    @Override
    public void update(Element el)
    {
        final Element remoteMacro = el
                .addElement("remote-macro")
                .addAttribute("key", key);

        addAttribute(remoteMacro, "url", path);
        addAttribute(remoteMacro, "name", name);
//        addAttribute(remoteMacro, "title", title);
        addAttribute(remoteMacro, "icon-url", iconUrl);
        addAttribute(remoteMacro, "output-type", outputType);
        addAttribute(remoteMacro, "body-type", bodyType);
        addAttribute(remoteMacro, "featured", featured);

        addElement(remoteMacro, category);
        addElement(remoteMacro, editor);
        addElement(remoteMacro, imagePlaceHolder);
        addElements(remoteMacro, parameters, "parameters");
        addElements(remoteMacro, contextParameters, "context-parameters");
    }

    @Override
    public Option<Pair<String, HttpServlet>> getResource()
    {
        if (path.isDefined() && servlet.isDefined())
        {
            return some(pair(path.get(), servlet.get()));
        }
        else
        {
            return none();
        }
    }
}
