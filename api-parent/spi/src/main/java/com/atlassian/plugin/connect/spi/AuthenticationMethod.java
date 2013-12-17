package com.atlassian.plugin.connect.spi;

import org.apache.commons.lang.StringUtils;

public enum AuthenticationMethod
{
    OAUTH1, JWT, NONE;

    public static final String PROPERTY_NAME = "atlassian.auth.method";

    public static AuthenticationMethod forName(String name)
    {
        return AuthenticationMethod.valueOf(StringUtils.upperCase(name));
    }
}
