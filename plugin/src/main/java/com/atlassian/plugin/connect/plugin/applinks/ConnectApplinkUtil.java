package com.atlassian.plugin.connect.plugin.applinks;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.spi.AuthenticationMethod;

public final class ConnectApplinkUtil
{
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

        else
        {
            Object oAuthConsumerKey = applink.getProperty(OAuthLinkManager.OAUTH_INCOMING_CONSUMERKEY);
            if (oAuthConsumerKey != null)
            {
                return Option.some(AuthenticationType.OAUTH);
            }
        }

        return Option.none();
    }

}
