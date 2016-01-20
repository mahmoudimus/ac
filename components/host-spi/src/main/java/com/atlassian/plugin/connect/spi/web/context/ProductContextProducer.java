package com.atlassian.plugin.connect.spi.web.context;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * This component takes a list of flat query parameters and loads
 * real objects if possible, returning them in a separate map.
 *
 * <p>
 *     For example, a producer may take a map with {@code issue.id=1340}
 *     and return a map with an entry {@code issue=Issue(id=1340)}
 *     (an actual issue object as a value).
 * </p>
 *
 * <p>
 *     This is needed for inline conditions in web items
 *     to work as they require a product-specific context.
 * </p>
 */
public interface ProductContextProducer
{
    Map<String, Object> produce(HttpServletRequest request, Map<String, String> queryParams);
}
