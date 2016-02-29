package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModesBean;

import java.util.Map;

/**
 * A remote Confluence macro type that is rendered within an iframe.
 */
public final class DynamicContentMacro extends AbstractMacro {
    private final RemoteMacroRenderer remoteMacroRenderer;
    private final String addonKey;
    private final String moduleKey;
    private final MacroRenderModesBean renderModes;

    public DynamicContentMacro(String addonKey, String moduleKey, BodyType bodyType, OutputType outputType,
                               RemoteMacroRenderer remoteMacroRenderer, MacroRenderModesBean renderModes) {
        super(bodyType, outputType);
        this.remoteMacroRenderer = remoteMacroRenderer;
        this.addonKey = addonKey;
        this.moduleKey = moduleKey;
        this.renderModes = renderModes;
    }

    @Override
    public String execute(Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext)
            throws MacroExecutionException {
        return remoteMacroRenderer.executeDynamic(addonKey, moduleKey, renderModes,
                parameters, storageFormatBody, conversionContext);
    }

}
