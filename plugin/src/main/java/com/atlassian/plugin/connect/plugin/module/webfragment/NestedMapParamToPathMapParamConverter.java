package com.atlassian.plugin.connect.plugin.module.webfragment;

import java.util.Map;

/**
 * Converts between a map of params in json style form
 * [project -> [id -> 10; key -> "foo"]]
 * and a map of individual params in path expression form
 * [project.id -> 10; project.key -> "foo"]
 */
public class NestedMapParamToPathMapParamConverter
{
    public Map<String, Object> convertToNestedForm(Map<String, String[]> pathParams)
    {
        return null;
    }

    public Map<String, String[]> convertToPathForm(Map<String, Object> nestedParams)
    {
        return null;
    }
}
