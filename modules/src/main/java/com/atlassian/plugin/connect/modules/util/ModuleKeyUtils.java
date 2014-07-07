package com.atlassian.plugin.connect.modules.util;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;

/**
 * Utility class to help with module key generation and conversion.
 * None of our module beans should expose a key and instead we should be using this utlity everywhere we need a module key.
 */
public class ModuleKeyUtils
{
    public static final String ADDON_MODULE_SEPARATOR = "__";
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generates a key using the given prefix and a random number.
     *
     * @param prefix
     * @return a key in the format: prefix-random, e.g. somemodule-890234325
     */
    public static String generateKey(String prefix)
    {
        return randomName(camelCaseOrSpaceToDashed(cleanKey(prefix)) + "-");
    }

    /**
     * Collapses CamelCase into dashes, replaces any special characters in a key with dashes and lowercases the entire
     * key.
     */
    public static String cleanKey(String originalKey)
    {
        return camelCaseOrSpaceToDashed(originalKey).replaceAll("[^a-zA-Z0-9\\-]", "-");
    }

    public static String camelCaseOrSpaceToDashed(String s)
    {
        String dashed = Joiner.on("-").join(s.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"));
        String trimmed = StringUtils.replace(StringUtils.replace(StringUtils.replace(dashed, " -", "-"), "- ", "-"), " ", "-");

        return trimmed.toLowerCase();
    }

    public static String addonAndModuleKey(String addonKey, String moduleKey)
    {
        return addonKey + ADDON_MODULE_SEPARATOR + cleanKey(moduleKey);
    }

    public static String moduleKeyOnly(String moduleKey)
    {
        return StringUtils.substringAfterLast(moduleKey, ADDON_MODULE_SEPARATOR);
    }

    public static String addonKeyOnly(String moduleKey)
    {
        return StringUtils.substringBeforeLast(moduleKey, ADDON_MODULE_SEPARATOR);
    }
    
    public static String toCompleteKey(String moduleKey)
    {
        return addonKeyOnly(moduleKey) + ":" + moduleKeyOnly(moduleKey);    
    }
    
    public static String randomName(String base)
    {
        long n = random.nextLong();
        n = (n == Long.MIN_VALUE) ? 0 : Math.abs(n);

        return base + Long.toString(n);
    }
}
