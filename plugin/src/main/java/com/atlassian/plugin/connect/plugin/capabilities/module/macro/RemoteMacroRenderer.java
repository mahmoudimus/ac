package com.atlassian.plugin.connect.plugin.capabilities.module.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModeType;

import java.util.Map;

/**
 * Render a macro to html.
 */
public interface RemoteMacroRenderer
{
    /**
     * Render a macro using the dynamic macro rendering logic.  The result will render an iframe for render
     * modes that support it ({@link com.atlassian.confluence.content.render.xhtml.ConversionContext#getOutputType},
     * otherwise it will call the static rendering fallback if one is defined.
     *
     * @param addOnKey the add on key
     * @param moduleKey the module key
     * @param renderModeUriTemplates the map of render modes
     * @param parameters the parameters to the macro
     * @param storageFormatBody the body of the macro
     * @param conversionContext the conversion context for this rendering
     * @return the html output
     * @throws MacroExecutionException
     */
    String executeDynamic(String addOnKey, String moduleKey, Map<MacroRenderModeType,String> renderModeUriTemplates,
                          Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext)
            throws MacroExecutionException;

    /**
     *
     * @param addOnKey the add on key
     * @param moduleKey the module key
     * @param uriTemplate the uri template for the remote macro
     * @param parameters the parameters to the macro
     * @param storageFormatBody the body of the macro
     * @param conversionContext the conversion context for this rendering
     * @return the static html rendering
     * @throws MacroExecutionException
     */
    String executeStatic(String addOnKey, String moduleKey, String uriTemplate,
                         Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext)
                    throws MacroExecutionException;
}
