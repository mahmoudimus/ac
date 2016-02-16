package com.atlassian.plugin.connect.test.common.servlet;

import java.util.Map;

/**
 * Extracts a map of key,values from the given String.
 */
public interface BodyExtractor
{
    Map<String,String> extractAll(String body);
}
