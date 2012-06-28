package junit;

import com.atlassian.labs.remoteapps.apputils.OAuthContext;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 28/06/12 Time: 11:57 PM To change this template use
 * File | Settings | File Templates.
 */
public class OAuthContextAccessor
{
    private static OAuthContext oAuthContext;

    public OAuthContextAccessor(OAuthContext oAuthContext)
    {
        this.oAuthContext = oAuthContext;
    }


    public static OAuthContext getOAuthContext()
    {
        return oAuthContext;
    }
}
