package com.atlassian.labs.remoteapps.api.service.http;

import java.util.List;

/**
 * Builds HTTP request entity strings for the application/x-www-form-urlencoded content-type.
 */
public interface FormBuilder
{
    /**
     *
     */
    public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    /**
     *
     *
     * @param name
     * @return
     */
    public FormBuilder addParam(String name);

    /**
     *
     *
     * @param name
     * @param value
     * @return
     */
    public FormBuilder addParam(String name, String value);

    /**
     *
     *
     * @param name
     * @param values
     * @return
     */
    public FormBuilder setParam(String name, List<String> values);

    /**
     *
     *
     * @return
     */
    public String toEntity();

    /**
     *
     *
     * @return
     */
    public String toString();
}
