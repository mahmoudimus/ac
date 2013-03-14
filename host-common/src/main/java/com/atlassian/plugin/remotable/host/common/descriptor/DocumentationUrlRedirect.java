package com.atlassian.plugin.remotable.host.common.descriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 */
public class DocumentationUrlRedirect
{
    private static final Logger log = LoggerFactory.getLogger(DocumentationUrlRedirect.class);
    public static boolean redirect(Map<String, String> pluginParams, HttpServletResponse resp) throws IOException
    {
        String docUrl = pluginParams.get("documentation.url");
        if (docUrl != null)
        {
            log.debug("Redirecting to documentation url '{}'", docUrl);
            resp.sendRedirect(docUrl);
            return true;
        }
        return false;
    }
}
