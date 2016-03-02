package it.com.atlassian.plugin.connect.util.request;

import com.atlassian.fugue.Iterables;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.api.request.HttpHeaderNames;
import com.atlassian.plugin.connect.testsupport.filter.ServletRequestSnapshot;

public final class HeaderUtil {
    public static Option<String> getVersionHeader(final ServletRequestSnapshot request) {
        Option<String> maybeHeader = Iterables.findFirst(request.getHeaders().keySet(), input -> input.equalsIgnoreCase(HttpHeaderNames.ATLASSIAN_CONNECT_VERSION));
        return maybeHeader.flatMap(headerName -> Option.option(request.getHeaders().get(headerName)));
    }
}
