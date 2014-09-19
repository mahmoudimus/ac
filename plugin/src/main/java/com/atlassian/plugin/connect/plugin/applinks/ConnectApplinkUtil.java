package com.atlassian.plugin.connect.plugin.applinks;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.fugue.Option;
import com.atlassian.jwt.JwtConstants;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.spi.AuthenticationMethod;

public final class ConnectApplinkUtil
{
    public static Option<AuthenticationType> getAuthenticationType(ApplicationLink applink)
    {
        Object authMethod = applink.getProperty(AuthenticationMethod.PROPERTY_NAME);
        if(AuthenticationMethod.JWT.toString().equals(authMethod))
        {
            return Option.some(AuthenticationType.JWT);
        }
        else if (AuthenticationMethod.OAUTH1.toString().equals(authMethod))
        {
            return Option.some(AuthenticationType.OAUTH);
        }
        else if (AuthenticationMethod.NONE.toString().equals(authMethod))
        {
            return Option.some(AuthenticationType.NONE);
        }
        
        return Option.none();
    }
    
    public static Option<String> getSharedSecretOrPublicKey(ApplicationLink applink)
    {
        Option<AuthenticationType> maybeAuthType = getAuthenticationType(applink);
        if(maybeAuthType.isDefined()) 
        {
            if(maybeAuthType.get().equals(AuthenticationType.JWT))
            {
                Object prop = applink.getProperty(JwtConstants.AppLinks.SHARED_SECRET_PROPERTY_NAME);
                if(prop instanceof String)
                {
                    return Option.some((String)prop);
                }
            }
            else if(maybeAuthType.get().equals(AuthenticationType.OAUTH))
            {
                //TODO
            }
        }
        return Option.none();
    }

}
