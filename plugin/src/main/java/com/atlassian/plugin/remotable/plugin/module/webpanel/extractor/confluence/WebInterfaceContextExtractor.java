package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.confluence;

import com.atlassian.confluence.plugin.descriptor.web.DefaultWebInterfaceContext;
import com.google.common.base.Function;
import com.google.common.base.Optional;

import java.util.Map;

public class WebInterfaceContextExtractor
{
    private static final String WEB_INTERFACE_CONTEXT = "webInterfaceContext";

    static Optional<Map.Entry<String, String[]>> extractFromWebInterfaceContext(
            final Map<String, Object> context,
            final Function<DefaultWebInterfaceContext, Map.Entry<String, String[]>> contextExtractorFunction)
    {
        if (context.containsKey(WEB_INTERFACE_CONTEXT))
        {
            final DefaultWebInterfaceContext webInterfaceContext = (DefaultWebInterfaceContext) context.get(WEB_INTERFACE_CONTEXT);
            return Optional.fromNullable(contextExtractorFunction.apply(webInterfaceContext));
        }
        else
        {
            return Optional.absent();
        }
    }


}
