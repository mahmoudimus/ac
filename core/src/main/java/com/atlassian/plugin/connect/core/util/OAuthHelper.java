package com.atlassian.plugin.connect.core.util;

import com.atlassian.oauth.util.Check;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import net.oauth.OAuth;
import net.oauth.OAuthMessage;

import static com.google.common.collect.Iterables.transform;

/**
 * Helps with oauth operations
 */
public class OAuthHelper
{
    /**
     * Converts the {@code Request} to an {@code OAuthMessage}.
     *
     * @param request {@code Request} to be converted to an {@code OAuthMessage}
     * @return {@code OAuthMessage} converted from the {@code Request}
     */
    public static OAuthMessage asOAuthMessage(final com.atlassian.oauth.Request request)
    {
        Check.notNull(request, "request");
        return new OAuthMessage(
            request.getMethod().name(),
            request.getUri().toString(),
            // We'd rather not do the copy, but since we need a Collection of these things we don't have much choice
            ImmutableList.copyOf(asOAuthParameters(request.getParameters()))
        );
    }

    /**
     * Converts the list of {@code Request.Parameter}s to {@code OAuth.Parameter}s.
     *
     * @param requestParameters {@code Request.Parameter}s to be converted to {@code OAuth.Parameter}s
     * @return {@code OAuth.Parameter}s converted from the {@code Request.Parameter}s
     */
    public static Iterable<OAuth.Parameter> asOAuthParameters(final Iterable<com.atlassian.oauth.Request.Parameter> requestParameters)
    {
        Check.notNull(requestParameters, "requestParameters");
        return transform(requestParameters, toOAuthParameters);
    }

    private static final Function<com.atlassian.oauth.Request.Parameter, OAuth.Parameter> toOAuthParameters = new Function<com.atlassian.oauth.Request.Parameter, OAuth.Parameter>()
    {
        public OAuth.Parameter apply(final com.atlassian.oauth.Request.Parameter p)
        {
            Check.notNull(p, "parameter");
            return new OAuth.Parameter(p.getName(), p.getValue());
        }
    };
}
