package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.Format;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.RequiresFormat;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *
 */
public class RemoteMacro implements Macro
{
    private final XhtmlContent xhtmlUtils;
    private final BodyType bodyType;
    private final OutputType outputType;
    private final String remoteUrl;
    private final ApplicationLinkOperationsFactory.LinkOperations linkOps;
    private final MacroContentManager macroContentManager;
    private final Logger log = LoggerFactory.getLogger(RemoteMacro.class);

    public RemoteMacro(XhtmlContent xhtmlUtils, BodyType bodyType, OutputType outputType, String remoteUrl, ApplicationLinkOperationsFactory.LinkOperations linkOps, MacroContentManager macroContentManager)
    {
        this.xhtmlUtils = xhtmlUtils;
        this.bodyType = bodyType;
        this.outputType = outputType;
        this.remoteUrl = remoteUrl;
        this.linkOps = linkOps;
        this.macroContentManager = macroContentManager;
    }

    @Override
    public BodyType getBodyType()
    {
        return bodyType;
    }

    @Override
    public OutputType getOutputType()
    {
        return outputType;
    }

    @Override
    @RequiresFormat(Format.Storage)
    public String execute(Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext) throws MacroExecutionException
    {
        String storageFormatContent = macroContentManager.getStaticContent(new MacroInstance(conversionContext.getEntity().getIdAsString(), remoteUrl, storageFormatBody, parameters, linkOps));

        try
        {
            return xhtmlUtils.convertStorageToView(storageFormatContent, conversionContext);
        }
        catch (Exception e)
        {
            log.error("Error converting macro content", e);
            return "Unable to retrieve and convert macro content: " + e.getMessage();
        }
    }
}
