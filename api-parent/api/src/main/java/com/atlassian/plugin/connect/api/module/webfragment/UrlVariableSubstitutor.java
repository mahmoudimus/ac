package com.atlassian.plugin.connect.api.module.webfragment;

import java.util.Map;

/**
 * Substitutes strings with variables defined with those defined in a given context.
 * <p>
 * Variables are in the form {var.name}, and are looked up in a nested map.
 * <p>
 * For example, given the source string "hi={user.name}" and a context such as
 * createMapOf("user", createMapOf("name", "joe")), {@link UrlVariableSubstitutor#replace(String, java.util.Map)}
 * would return "hi=joe".
 * <p>
 * All values in the context are percent-encoded for subsitution into a URL.
 * <p>
 * Variables that that cannot be found in the map are replaced by an empty string. For example,
 * given the source String "hi={foo.bar}" and an empty map, {@link UrlVariableSubstitutor#replace(String, java.util.Map)}
 * would return "hi="
 */
public interface UrlVariableSubstitutor
{
    /**
     * Replaces all variables in the given source with values from the given context.
     * @param source string containing variables
     * @param context context containing values to replace
     * @return source with variables replaced by values.
     */
    public String replace(String source, Map<String, ?> context);

    /**
     * Appends (rather than substitutes) a map of parameters to the end of the url.
     *
     * @param source the original URL
     * @param parameters the parameters to append
     * @return the URL, with the supplied parameters appended
     */
    public String append(String source, Map<String, String> parameters);

    /**
     * Parses from the given URL a {@link java.util.Map} of name-in-source to context-variable-name.
     * @param source string containing variables (e.g. "http://server:80/path?my_page_id={page.id}" or "my_page_id={page.id}")
     * @return {@link java.util.Map} of name-in-source to context-variable-name (e.g. "my_page_id" to "page.id")
     */
    public Map<String, String> getContextVariableMap(final String source);
}
