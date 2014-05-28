package com.atlassian.plugin.connect.modules.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

/**
 * Class used to sanitize strings by removing ${} and just leaving the variable names.
 * This is used by anything that needs to pass descriptor string values to a method that renders velocity. e.g. WebLabel
 */
public class VelocityKiller
{
    public static final Pattern VELOCITY_VAR_PATTERN = Pattern.compile("\\$(\\{([a-zA-Z0-9\\-_\\.\\(\\)]+)\\}|([a-zA-Z0-9\\-_\\.\\(\\)\\!]+))");
    
    public static String attack(String rawValue)
    {
        if(Strings.isNullOrEmpty(rawValue) || rawValue.length() < 2)
        {
            return rawValue;
        }
        
        Matcher matcher = VELOCITY_VAR_PATTERN.matcher(rawValue);
        
        return matcher.replaceAll("\\\\\\$$1");
    }
}
