package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.Format;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.RequiresFormat;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.labs.remoteapps.ContentRetrievalException;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *
 */
public class StorageFormatMacro extends AbstractRemoteMacro
{
    private final XhtmlContent xhtmlUtils;
    private final String remoteUrl;
    private final MacroContentManager macroContentManager;
    private final Logger log = LoggerFactory.getLogger(StorageFormatMacro.class);

    public StorageFormatMacro(RemoteMacroInfo remoteMacroInfo, XhtmlContent xhtmlUtils,
            MacroContentManager macroContentManager)
    {
        super(remoteMacroInfo);
        this.xhtmlUtils = xhtmlUtils;
        this.remoteUrl = remoteMacroInfo.getUrl();
        this.macroContentManager = macroContentManager;
    }

    @Override
    @RequiresFormat(Format.Storage)
    public String execute(Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext) throws MacroExecutionException
    {
        String storageFormatContent;
        ApplicationLinkOperationsFactory.LinkOperations linkOps = remoteMacroInfo.getApplicationLinkOperations();
        try
        {
            storageFormatContent = macroContentManager.getStaticContent(new MacroInstance(conversionContext, remoteUrl, storageFormatBody, parameters, linkOps));
        }
        catch (ContentRetrievalException ex)
        {
            String message = "ERROR: Unable to retrieve macro content from Remote App '" + linkOps.get().getName() + "': " + ex.getMessage();
            log.error(message + " on page '{}' for url '{}'", conversionContext.getEntity().getTitle(), remoteUrl);
            if (log.isDebugEnabled())
            {
                log.debug("Unable to retrieve content", ex);
            }
            return message;
        }

        try
        {
            return xhtmlUtils.convertStorageToView(storageFormatContent, conversionContext);
        }
        catch (Exception e)
        {
            String message = "ERROR: Unable to convert macro content from Remote App '" + linkOps.get().getName() + "': " + e.getMessage();
            log.error(message + " on page {}", conversionContext.getEntity().getTitle());
            return message;
        }
    }

}
