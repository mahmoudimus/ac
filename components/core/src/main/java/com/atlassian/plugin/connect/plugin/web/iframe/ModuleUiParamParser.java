package com.atlassian.plugin.connect.plugin.web.iframe;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * A parser for UI specific request parameters.
 *
 * UI parameters is an opaque (to the server) object which the client code can pass in the request and have the server
 * return it as part of the signed iFrame url. As it has it's own query parameter "ui-params" the contents should not
 * be confused with the resource keys (e.g. issue.key) so we don't need to validate them even though they end up
 * in the signed url
 */

public interface ModuleUiParamParser {
    Optional<String> parseUiParameters(HttpServletRequest req);
}
