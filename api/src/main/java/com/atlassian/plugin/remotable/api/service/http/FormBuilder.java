package com.atlassian.plugin.remotable.api.service.http;

import java.util.List;

/**
 * Builds url-encoded form entities for use as HTTP request message bodies.
 * URL encoding of parameter names and values is handled by FormBuilder implementations.
 */
public interface FormBuilder extends EntityBuilder
{
    /**
     * Adds a valueless parameter.
     *
     * @param name The name of the parameter
     * @return This object, for builder-style chaining
     */
    public FormBuilder addParam(String name);

    /**
     * Adds a parameter and its value.
     *
     * @param name The name of the parameter
     * @param value The value of the parameter
     * @return This object, for builder-style chaining
     */
    public FormBuilder addParam(String name, String value);

    /**
     * Sets multiple values for the named parameter, resetting any existing values in the process.
     *
     * @param name The name of the parameter
     * @param values A list of all values for the named the parameter
     * @return This object, for builder-style chaining
     */
    public FormBuilder setParam(String name, List<String> values);
}
