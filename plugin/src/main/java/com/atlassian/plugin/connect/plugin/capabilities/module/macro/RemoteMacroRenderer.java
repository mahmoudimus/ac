package com.atlassian.plugin.connect.plugin.capabilities.module.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModeType;

import java.util.Map;

/**
 * Created by mjensen on 19/11/2014.
 */
public interface RemoteMacroRenderer
{
    String executeDynamic(String addOnKey, String moduleKey, Map<MacroRenderModeType,String> renderModeUriTemplates,
                          Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext)
            throws MacroExecutionException;

    String executeStatic(String addOnKey, String moduleKey, String uriTemplate,
                         Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext)
                    throws MacroExecutionException;
}
