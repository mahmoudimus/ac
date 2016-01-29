package com.atlassian.plugin.connect.plugin.auth;

import com.atlassian.jwt.JwtConstants;
import org.apache.commons.lang.StringUtils;

public enum AuthenticationMethod
{
    OAUTH1, JWT, NONE;

    public static final String PROPERTY_NAME = JwtConstants.AppLinks.AUTH_METHOD_PROPERTY_NAME;

    public static AuthenticationMethod forName(String name)
    {
        return AuthenticationMethod.valueOf(StringUtils.upperCase(name));
    }
}
