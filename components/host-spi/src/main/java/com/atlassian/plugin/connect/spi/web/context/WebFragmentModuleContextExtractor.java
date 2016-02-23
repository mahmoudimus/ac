package com.atlassian.plugin.connect.spi.web.context;

import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * @since 1.0
 */
public interface WebFragmentModuleContextExtractor {
    /**
     * Some connect modules will compute their own {@link ModuleContextParameters}. To prevent writing the context
     * extraction logic twice, the module may place the parsed context in the context map under this key. Context
     * extractor implementations will then merge the supplied context map with any other extracted context.
     */
    String MODULE_CONTEXT_KEY = "acModuleContext";

    /**
     * Takes a web fragment context and extracts parameters to be consumed by Connect add-ons.
     */
    ModuleContextParameters extractParameters(Map<String, ? extends Object> webFragmentContext);

    /**
     * Reverses the {@link WebFragmentModuleContextExtractor#extractParameters(Map)} method.
     */
    Map<String, Object> reverseExtraction(HttpServletRequest request, Map<String, String> queryParams);
}
