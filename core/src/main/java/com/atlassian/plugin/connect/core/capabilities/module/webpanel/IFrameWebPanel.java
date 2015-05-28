package com.atlassian.plugin.connect.core.capabilities.module.webpanel;

import com.atlassian.plugin.connect.api.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.core.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.spi.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Web panel that displays in an iframe.
 */
public class IFrameWebPanel implements WebPanel
{
    private static final Logger log = LoggerFactory.getLogger(IFrameWebPanel.class);

    private final IFrameRenderer iFrameRenderer;
    private final IFrameContext iFrameContext;
    private final ContextMapURLSerializer contextMapURLSerializer;
    private final UserManager userManager;
    private final Condition condition;
    private final UrlVariableSubstitutor urlVariableSubstitutor;

    public IFrameWebPanel(
            IFrameRenderer iFrameRenderer,
            IFrameContext iFrameContext,
            Condition condition,
            ContextMapURLSerializer contextMapURLSerializer,
            UserManager userManager, UrlVariableSubstitutor urlVariableSubstitutor)
    {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.userManager = checkNotNull(userManager);
        this.contextMapURLSerializer = checkNotNull(contextMapURLSerializer);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.iFrameContext = checkNotNull(iFrameContext);
        this.condition = checkNotNull(condition);
    }

    @Override
    public String getHtml(final Map<String, Object> context)
    {
        StringWriter writer = new StringWriter();
        try
        {
            writeHtml(writer, context);
        }
        catch (IOException e)
        {
            writer.write("Unable to render panel: " + e.getMessage());
            log.error("Error rendering panel", e);
        }
        return writer.toString();
    }

    @Override
    public void writeHtml(final Writer writer, final Map<String, Object> context) throws IOException
    {
        if (condition.shouldDisplay(context))
        {
            UserProfile remoteUser = userManager.getRemoteUser();
            String remoteUsername = remoteUser == null ? "" : remoteUser.getUsername();

            final Map<String, Object> whiteListedContext = contextMapURLSerializer.getExtractedWebPanelParameters(context);

            writer.write(iFrameRenderer.render(substituteContext(whiteListedContext), "", Collections.EMPTY_MAP, remoteUsername, whiteListedContext));
        }
        else
        {
            writer.write("Unauthorized to view this panel");
            log.error("Unauthorized view of panel");
        }
    }

    private IFrameContext substituteContext(Map<String, Object> whiteListedContext)
    {
        return new IFrameContextImpl(iFrameContext.getPluginKey(),
                urlVariableSubstitutor.replace(iFrameContext.getIframePath(), whiteListedContext),
                iFrameContext.getNamespace(),
                iFrameContext.getIFrameParams());
    }
}
