package com.atlassian.plugin.connect.plugin.module.context;

import java.util.Map;

/**
 * <p>Implementations of this interface are supposed to extract the parameters from web-panel's context to parameters which
 * will be included in URL of remote-web-panel iframe. </p>
 * <p> E.g., in order to include a page.id in a web-panel's iframe URL, we return a map of ("page", ("id", page_id)).</p>
 * <p>Components implementing this interface should be product specific.</p>
 */
public interface ContextMapParameterExtractor<P> extends ParameterExtractor<Map<String, Object>, P>
{
}
