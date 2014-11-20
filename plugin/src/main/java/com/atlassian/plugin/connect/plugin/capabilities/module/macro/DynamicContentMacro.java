package com.atlassian.plugin.connect.plugin.capabilities.module.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModeType;

import java.util.Map;

/**
 * A remote Confluence macro type that is rendered within an iframe.
 */
public final class DynamicContentMacro extends AbstractMacro
{
    private final RemoteMacroRenderer remoteMacroRenderer;
    private final String addOnKey;
    private final String moduleKey;
    private final Map<MacroRenderModeType, String> renderModeUriTemplates;

    public DynamicContentMacro(String addOnKey, String moduleKey, BodyType bodyType, OutputType outputType,
                               RemoteMacroRenderer remoteMacroRenderer, Map<MacroRenderModeType, String> renderModeUriTemplates)
    {
        super(bodyType, outputType);
        this.remoteMacroRenderer = remoteMacroRenderer;
        this.addOnKey = addOnKey;
        this.moduleKey = moduleKey;
        this.renderModeUriTemplates = renderModeUriTemplates;
    }

    @Override
    public String execute(Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext)
            throws MacroExecutionException
    {
        return remoteMacroRenderer.executeDynamic(addOnKey, moduleKey, renderModeUriTemplates,
                parameters, storageFormatBody,conversionContext);
    }

}
