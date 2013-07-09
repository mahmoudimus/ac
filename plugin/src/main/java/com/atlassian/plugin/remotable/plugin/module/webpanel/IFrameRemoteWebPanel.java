package com.atlassian.plugin.remotable.plugin.module.webpanel;

import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.WebPanelAllParametersExtractor;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.plugin.remotable.spi.module.IFrameContext;
import com.atlassian.plugin.remotable.spi.module.IFrameRenderer;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Objects;
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
    private final boolean hiddenByDefault;
    private final WebPanelAllParametersExtractor webPanelAllParametersExtractor;
    private final UserManager userManager;

    public IFrameRemoteWebPanel(
            final IFrameRenderer iFrameRenderer,
            final IFrameContext iFrameContext,
            final boolean hiddenByDefault,
            final WebPanelAllParametersExtractor webPanelAllParametersExtractor,
            final UserManager userManager)
    {
        this.userManager = checkNotNull(userManager);
        this.webPanelAllParametersExtractor = checkNotNull(webPanelAllParametersExtractor);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.iFrameContext = checkNotNull(iFrameContext);
        this.hiddenByDefault = checkNotNull(hiddenByDefault);
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
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

    @Override
    public void writeHtml(final Writer writer, final Map<String, Object> context) throws IOException
    {
        try
        {
            final String remoteUser = Objects.firstNonNull(userManager.getRemoteUsername(), "");
            final Map<String,String[]> params = webPanelAllParametersExtractor.getExtractedWebPanelParameters(context);

            String iframe = iFrameRenderer.render(iFrameContext, "", params, remoteUser);
            if (hiddenByDefault)
            {
                iframe = "<script>AJS.$('#" + iFrameContext.getNamespace() + "').addClass('hidden');</script>" + iframe;
            }
            writer.write(iframe);
        }
        catch (PermissionDeniedException ex)
        {
            writer.write("Unauthorized to view this panel");
            log.warn("Unauthorized view of panel");
        }
        catch (IOException e)
        {
            writer.write("Unable to render panel: " + e.getMessage());
            log.error("Error rendering panel", e);
        }
    }
}
