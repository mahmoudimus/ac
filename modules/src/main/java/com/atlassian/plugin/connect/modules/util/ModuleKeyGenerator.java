package com.atlassian.plugin.connect.modules.util;

import java.security.SecureRandom;

import com.google.common.base.Joiner;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class to help with module key generation and conversion.
 * None of our module beans should expose a key and instead we should be using this utlity everywhere we need a module key.
 */
public class ModuleKeyGenerator
{
    private static final SecureRandom random = new SecureRandom();
    public static String CLEAN_FILENAME_PATTERN = "[:\\\\/*?|<>_]";

    /**
     * Generates a key using the given prefix and a random number.
     *
     * @param prefix
     * @return a key in the format: prefix-random, e.g. somemodule-890234325
     */
    public static String generateKey(String prefix)
    {
        return randomName(camelCaseOrSpaceToDashed(prefix) + "-");
    }

    /**
     * Converts a name to a key by replacing all whitespace with dashes and lowercasing the whole string
     *
     * @param name
     * @return "My Module" -> my-module
     */
    public static String nameToKey(String name)
    {
        return randomName(camelCaseOrSpaceToDashed(name));
    }

    /**
     * Converts a name to a key using the given prefix, replacing all whitespace with dashes and lowercasing the whole string
     *
     * @param prefix
     * @param name
     * @return "web-item","My Module" -> web-item
     */
    public static String nameToKey(String prefix, String name)
    {
        return randomName(camelCaseOrSpaceToDashed(prefix) + "-" + camelCaseOrSpaceToDashed(name));
    }

    public static String camelCaseOrSpaceToDashed(String s)
    {
        String dashed = Joiner.on("-").join(s.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"));
        String trimmed = StringUtils.replace(StringUtils.replace(StringUtils.replace(dashed, " -", "-"), "- ", "-"), " ", "-");

        return trimmed.toLowerCase();
    }

    private static String randomName(String base)
    {
        long n = random.nextLong();
        n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);

        return base + Long.toString(n);
    }
}
