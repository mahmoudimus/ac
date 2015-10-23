package it.com.atlassian.plugin.connect.util.request;

import com.atlassian.fugue.Iterables;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.request.HttpHeaderNames;
import com.atlassian.plugin.connect.testsupport.filter.ServletRequestSnapshot;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

public final class HeaderUtil
{
    public static Option<String> getVersionHeader(final ServletRequestSnapshot request)
    {
        Option<String> maybeHeader = Iterables.findFirst(request.getHeaders().keySet(), new Predicate<String>()
        {
            @Override
            public boolean apply(String input)
            {
                return input.equalsIgnoreCase(HttpHeaderNames.ATLASSIAN_CONNECT_VERSION);
            }
        });
        return maybeHeader.flatMap(new Function<String, Option<String>>()
        {
            @Override
            public Option<String> apply(String headerName)
            {
                return Option.option(request.getHeaders().get(headerName));
            }
        });
    }
}
