package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor;

import java.util.Map;

/**
 * <p>Implementations of this interface are supposed to extract the parameters from web-panel's context to parameters which
 * will be included in URL of remote-web-panel iframe. </p>
 * <p> E.g., in order to include a page_id in a web-panel's iframe URL, we return a pair of ("page_id", page_id).</p>
 * <p>Components implementing this interface should be product specific.</p>
 */
public interface WebPanelParameterExtractor
{
    /**
     * Extracts the parameters from context and adds to a whitelisted context.
     *
     * @param context a web panel's context.
     * @param whiteListedContext a web panel's context.
     */
    void extract(final Map<String, Object> context, Map<String, Object> whiteListedContext);
}
