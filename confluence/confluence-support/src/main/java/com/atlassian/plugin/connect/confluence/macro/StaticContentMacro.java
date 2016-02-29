package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.Format;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.RequiresFormat;
import com.atlassian.confluence.macro.MacroExecutionException;

import java.util.Map;

public class StaticContentMacro extends AbstractMacro {
    private final String addonKey;
    private final String moduleKey;
    private final String uriTemplate;
    private final RemoteMacroRenderer remoteMacroRenderer;

    public StaticContentMacro(String addonKey, String moduleKey, String uriTemplate, BodyType bodyType, OutputType outputType, RemoteMacroRenderer remoteMacroRenderer) {
        super(bodyType, outputType);
        this.addonKey = addonKey;
        this.moduleKey = moduleKey;
        this.uriTemplate = uriTemplate;
        this.remoteMacroRenderer = remoteMacroRenderer;
    }

    @Override
    @RequiresFormat(Format.Storage)
    public String execute(Map<String, String> parameters, String body, ConversionContext context) throws MacroExecutionException {
        return remoteMacroRenderer.executeStatic(addonKey, moduleKey, uriTemplate, parameters, body, context);
    }
}
