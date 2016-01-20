package com.atlassian.plugin.connect.plugin.web.context;

import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.plugin.connect.spi.web.context.ProductContextProducer;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

@ConfluenceComponent
public class ConfluenceProductContextProducer implements ProductContextProducer
{
    @Override
    public Map<String, Object> produce(final HttpServletRequest request, final Map<String, String> queryParams)
    {
        return Collections.emptyMap();
    }
}
