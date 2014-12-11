package com.atlassian.plugin.connect.spi.module;

import java.util.Map;

/**
 * Components implementing this interface can provide
 * parameterss for Connect add-ons based on the current context.
 *
 * <p>
 *     Note that it's not enough to simply provide the parameters.
 *     Each parameter needs to pass a permission check.
 *     You can define permission checks by implementing {@link ContextParametersValidator}
 * </p>
 */
public interface ContextParametersExtractor
{
    /**
     * Given an execution context, extract parameters to be used by the Connect add-ons.
     *
     * <p>
     *     Execution context may contain arbitrary objects, e.g. a whole Issue. Connect
     *     add-ons on the other hand expect plain Strings. So an example provider could
     *     take a context of the form
     *
     *     <pre>
     *         {
     *             "issue": IssueObject[id = 1, type = "bug", ...]
     *         }
     *     </pre>
     *
     *     and return a Connect add-on friendly map:
     *
     *     <pre>
     *         {
     *             "issue.id": "1",
     *             "issue.type": "bug"
     *         }
     *     </pre>
     * </p>
     *
     * @param context execution context
     * @return context for Connect add-ons
     */
    public Map<String, String> extractParameters(Map<String, ? extends Object> context);
}
