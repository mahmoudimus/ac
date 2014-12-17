package com.atlassian.plugin.connect.plugin.capabilities.module.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.MacroExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class StaticContentMacro extends AbstractMacro
{
    private final String addOnKey;
    private final String moduleKey;
    private final String uriTemplate;
    private final RemoteMacroRenderer remoteMacroRenderer;

    public StaticContentMacro(String addOnKey, String moduleKey, String uriTemplate, BodyType bodyType, OutputType outputType, RemoteMacroRenderer remoteMacroRenderer)
    {
        super(bodyType, outputType);
        this.addOnKey = addOnKey;
        this.moduleKey = moduleKey;
        this.uriTemplate = uriTemplate;
        this.remoteMacroRenderer = remoteMacroRenderer;
    }

    @Override
    public String execute(Map<String, String> parameters, String body, ConversionContext context) throws MacroExecutionException
    {
        return remoteMacroRenderer.executeStatic(addOnKey, moduleKey, uriTemplate, parameters, body, context);
    }
}
