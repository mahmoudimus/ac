package com.atlassian.plugin.remotable.plugin.module.confluence;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.Format;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.RequiresFormat;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.remotable.plugin.RemotablePluginAccessor;
import com.atlassian.plugin.remotable.plugin.RemotablePluginAccessorFactory;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Map;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/*!
The `macro` module retrieves storage-format XML from the remote plugin and renders it.  This allows
the remote plugin to generate macro content that is:

1. Aggressively cached for performance
2. Able to use other macros in rendering
3. Safe for embedding directly in the page as the storage-format XML is ran through the same
   sanitizers used for the editor
 */

/*!-constructor and fields */
public class StorageFormatMacro extends AbstractRemoteMacro
{
    private final URI remoteUrl;
    private final MacroContentManager macroContentManager;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final WebResourceManager webResourceManager;
    private final Logger log = LoggerFactory.getLogger(StorageFormatMacro.class);

    public StorageFormatMacro(RemoteMacroInfo remoteMacroInfo,
            MacroContentManager macroContentManager,
            RemotablePluginAccessorFactory remotablePluginAccessorFactory,
            WebResourceManager webResourceManager)
    {
        super(remotablePluginAccessorFactory, remoteMacroInfo);
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.webResourceManager = webResourceManager;
        this.remoteUrl = remoteMacroInfo.getUrl();
        this.macroContentManager = macroContentManager;
    }

    /*!
    When the macro is rendered, it receives its body content as storage-format XML, not rendered
    HTML.  This allows the user to embed other macros in the body of the remote plugin macro.
     */
    @Override
    @RequiresFormat(Format.Storage)
    public String execute(Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext) throws MacroExecutionException
    {
        RemotablePluginAccessor remotablePluginAccessor = remotablePluginAccessorFactory.get(
                remoteMacroInfo.getPluginKey());
        /*!
        Next, the remotable plugins is called and its returned storage-format XML is rendered.  If the
        content was unable to be retrieved for any reason, an error message is displayed to the user
        as the macro content.  For more information on the macro content retrieval process, see the
        <a href="https://remoteapps.jira.com/wiki/display/ARA/Macro+Retrieval+Process" target="_top">macro retrieval process</a> walkthrough.
         */
        try
        {
            webResourceManager.requireResource("com.atlassian.labs.remoteapps-plugin:big-pipe");
            return macroContentManager.getStaticContent(
                    new MacroInstance(conversionContext, remoteUrl, storageFormatBody, parameters,
                            remoteMacroInfo.getRequestContextParameterFactory(), remotablePluginAccessor));
        }
        catch (Exception ex)
        {
            String exMessage = ex.getMessage();
            if (ex.getCause() != null && ex.getCause() instanceof SocketTimeoutException)
            {
                exMessage = "Timeout waiting for reply";
            }
            String message = "Error: Unable to retrieve macro content from Remotable Plugin '" + remotablePluginAccessor
                    .getName() + "': " + exMessage;
            log.error(message + " on page '{}' for url '{}'", escapeHtml(
                    conversionContext.getEntity().getTitle()), remoteUrl);
            if (log.isDebugEnabled())
            {
                log.debug("Unable to retrieve content", ex);
            }
            return message;
        }
    }

    @Override
    public RemotablePluginAccessor getRemotablePluginAccessor(String pluginKey)
    {
        return remotablePluginAccessorFactory.get(pluginKey);
    }
}
