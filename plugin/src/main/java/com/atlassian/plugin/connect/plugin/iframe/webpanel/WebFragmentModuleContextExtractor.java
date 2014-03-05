package com.atlassian.plugin.connect.plugin.iframe.webpanel;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;

import java.util.Map;

/**
 * @since 1.0
 */
public interface WebFragmentModuleContextExtractor
{
    /**
     * Some connect modules will compute their own {@link ModuleContextParameters}. To prevent writing the context
     * extraction logic twice, the module may place the parsed context in the context map under this key. Context
     * extractor implementations will then merge the supplied context map with any other extracted context.
     */
    String MODULE_CONTEXT_KEY = "acModuleContext";

    ModuleContextParameters extractParameters(Map<String, ? extends Object> webFragmentContext);
}
