package com.atlassian.plugin.connect.spi.web.context;

import java.util.Map;

/**
 * @since 1.0
 */
public interface WebFragmentModuleContextExtractor
{
    /**
     * Some connect modules will compute their own context parameters. To prevent writing the context
     * extraction logic twice, the module may place the parsed context in the context map under this key. Context
     * extractor implementations will then merge the supplied context map with any other extracted context.
     */
    String MODULE_CONTEXT_KEY = "acModuleContext";

    Map<String, String> extractParameters(Map<String, ? extends Object> webFragmentContext);
}
