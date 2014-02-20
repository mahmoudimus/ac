package com.atlassian.plugin.connect.plugin.module.confluence;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.Format;
import com.atlassian.confluence.content.render.xhtml.macro.annotation.RequiresFormat;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.util.LocaleHelper;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.google.common.collect.ImmutableMap;
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
    private final HttpMethod httpMethod;
    private final MacroContentManager macroContentManager;
    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final Logger log = LoggerFactory.getLogger(StorageFormatMacro.class);
    private final LicenseRetriever licenseRetriever;
    private final LocaleHelper localeHelper;

    public StorageFormatMacro(RemoteMacroInfo remoteMacroInfo,
                              MacroContentManager macroContentManager,
                              RemotablePluginAccessorFactory remotablePluginAccessorFactory,
                              LicenseRetriever licenseRetriever,
                              LocaleHelper localeHelper)
    {
        super(remotablePluginAccessorFactory, remoteMacroInfo);
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.remoteUrl = remoteMacroInfo.getUrl();
        this.httpMethod = remoteMacroInfo.getHttpMethod();
        this.macroContentManager = macroContentManager;
        this.licenseRetriever = licenseRetriever;
        this.localeHelper = localeHelper;
    }

    /*!
    When the macro is rendered, it receives its body content as storage-format XML, not rendered
    HTML.  This allows the user to embed other macros in the body of the remote plugin macro.
     */
    @Override
    @RequiresFormat(Format.Storage)
    public String execute(Map<String, String> macroParameters, String storageFormatBody, ConversionContext conversionContext) throws MacroExecutionException
    {
        final RemotablePluginAccessor remotablePluginAccessor = remotablePluginAccessorFactory.get(remoteMacroInfo.getPluginKey());
        /*!
        Next, the remotable plugins is called and its returned storage-format XML is rendered.  If the
        content was unable to be retrieved for any reason, an error message is displayed to the user
        as the macro content.  For more information on the macro content retrieval process, see the
        <a href="https://remoteapps.jira.com/wiki/display/ARA/Macro+Retrieval+Process" target="_top">macro retrieval process</a> walkthrough.
         */

        Map<String, String> parameters = getAllParameters(macroParameters, remoteMacroInfo.getPluginKey());

        try
        {
            return macroContentManager.getStaticContent(
                    new MacroInstance(conversionContext, remoteUrl, httpMethod, storageFormatBody, parameters,
                            remoteMacroInfo.getRequestContextParameterFactory(), remotablePluginAccessor));
        }
        catch (Exception ex)
        {
            String exMessage = ex.getMessage();
            if (ex.getCause() != null && ex.getCause() instanceof SocketTimeoutException)
            {
                exMessage = "Timeout waiting for reply";
            }
            final String message = "Error: Unable to retrieve macro content from Remotable Plugin '" + remotablePluginAccessor.getName() + "': " + exMessage;
            log.error(message + " on page '{}' for url '{}'", escapeHtml(conversionContext.getEntity().getTitle()), remoteUrl);
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

    private Map<String, String> getAllParameters(Map<String, String> parameters, String pluginKey)
    {
        return ImmutableMap.<String, String>builder()
                .putAll(parameters)
                .put("lic", getLicenseStatusAsString(pluginKey))
                .put("loc", getLocale())
                .build();
    }

    private String getLicenseStatusAsString(String pluginKey)
    {
        return licenseRetriever.getLicenseStatus(pluginKey).value();
    }

    private String getLocale()
    {
        return localeHelper.getLocaleTag();
    }
}
