package com.atlassian.plugin.connect.plugin.applinks;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.spi.AuthenticationMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConnectApplinkUtil
{

    private static final Logger log = LoggerFactory.getLogger(ConnectApplinkUtil.class);

    public static Option<AuthenticationType> getAuthenticationType(ApplicationLink applink)
    {
        Object authMethod = applink.getProperty(AuthenticationMethod.PROPERTY_NAME);
        if (AuthenticationMethod.JWT.toString().equals(authMethod))
        {
            return Option.some(AuthenticationType.JWT);
        }
        else if (AuthenticationMethod.NONE.toString().equals(authMethod))
        {
            return Option.some(AuthenticationType.NONE);
        }
        else if (authMethod != null)
        {
            log.warn("Unknown authType encountered: " + authMethod);
            return Option.some(AuthenticationType.NONE);
        }
        return Option.none();
    }
}
