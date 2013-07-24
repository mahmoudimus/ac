package com.atlassian.plugin.remotable.plugin.module.webpanel;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.plugin.remotable.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.remotable.plugin.module.webfragment.StringSubstitutor;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.WebPanelURLParametersSerializer;
import com.atlassian.plugin.remotable.spi.module.IFrameContext;
import com.atlassian.plugin.remotable.spi.module.IFrameRenderer;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Web panel that displays in an iframe.
 */
public class IFrameRemoteWebPanel implements WebPanel
{
    private static final Logger log = LoggerFactory.getLogger(IFrameRemoteWebPanel.class);

    private final IFrameRenderer iFrameRenderer;
    private final IFrameContext iFrameContext;
    private final WebPanelURLParametersSerializer webPanelURLParametersSerializer;
    private final UserManager userManager;
    private final Condition condition;
    private final StringSubstitutor stringSubstitutor;

    public IFrameRemoteWebPanel(
            IFrameRenderer iFrameRenderer,
            IFrameContext iFrameContext,
            Condition condition,
            WebPanelURLParametersSerializer webPanelURLParametersSerializer,
            UserManager userManager, StringSubstitutor stringSubstitutor)
    {
        this.stringSubstitutor = stringSubstitutor;
        this.userManager = checkNotNull(userManager);
        this.webPanelURLParametersSerializer = checkNotNull(webPanelURLParametersSerializer);
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
            final String remoteUser = StringUtils.defaultString(userManager.getRemoteUsername());
            final Map<String,String[]> params = webPanelURLParametersSerializer.getExtractedWebPanelParameters(context);

            writer.write(iFrameRenderer.render(substituteContext(context), "", params, remoteUser));
        }
        else
        {
            writer.write("Unauthorized to view this panel");
            log.error("Unauthorized view of panel");
        }
    }

    private IFrameContext substituteContext(Map<String, Object> context)
    {
        return new IFrameContextImpl(iFrameContext.getPluginKey(),
                stringSubstitutor.replace(iFrameContext.getIframePath(), context),
                iFrameContext.getNamespace(),
                iFrameContext.getIFrameParams());
    }
}
