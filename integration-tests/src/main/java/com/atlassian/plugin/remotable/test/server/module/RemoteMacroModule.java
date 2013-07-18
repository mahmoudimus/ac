package com.atlassian.plugin.remotable.test.server.module;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Pair;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.dom4j.Element;

import javax.servlet.http.HttpServlet;

import static com.atlassian.fugue.Option.none;
import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;

public final class RemoteMacroModule extends AbstractModuleWithResource<RemoteMacroModule>
{
    private final String key;
    private Option<String> name = none();
    private Option<String> title = none();
    private Option<String> iconUrl = none();
    private Option<String> outputType = none();
    private Option<String> bodyType = none();
    private Option<String> featured = none();
    private Option<MacroCategory> category = none();
    private Iterable<MacroParameter> parameters = ImmutableList.of();
    private Iterable<ContextParameter> contextParameters = ImmutableList.of();
    private Option<MacroEditor> editor = none();
    private Option<ImagePlaceHolder> imagePlaceHolder = none();

    private RemoteMacroModule(String key)
    {
        super("remote-macro");
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

    public RemoteMacroModule imagePlaceHolder(ImagePlaceHolder img)
    {
        this.imagePlaceHolder = option(img);
        return this;
    }

    @Override
    protected void addToElement(Element el)
    {
        addAttribute(el, "key", some(key));
        addAttribute(el, "url", path);
        addAttribute(el, "name", name);
//        addAttribute(remoteMacro, "title", title);
        addAttribute(el, "icon-url", iconUrl);
        addAttribute(el, "output-type", outputType);
        addAttribute(el, "body-type", bodyType);
        addAttribute(el, "featured", featured);

        addElement(el, category);
        addElement(el, editor);
        addElement(el, imagePlaceHolder);
        addElements(el, "parameters", parameters);
        addElements(el, "context-parameters", contextParameters);
    }

    @Override
    protected Iterable<? extends Pair<String, HttpServlet>> getSubResources()
    {
        return concat(getResources(editor), getResources(imagePlaceHolder));
    }

    private Iterable<? extends Pair<String, HttpServlet>> getResources(Option<? extends Module> module)
    {
        return module.map(new Function<Module, Iterable<? extends Pair<String, HttpServlet>>>()
        {
            @Override
            public Iterable<? extends Pair<String, HttpServlet>> apply(Module m)
            {
                return m.getResources();
            }
        }).getOrElse(ImmutableList.<Pair<String, HttpServlet>>of());
    }
}
