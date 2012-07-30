package junit;


import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 28/06/12 Time: 11:57 PM To change this template use
 * File | Settings | File Templates.
 */
public class SignedRequestHandlerAccessor
{
    private static SignedRequestHandler signedRequestHandler;

    public SignedRequestHandlerAccessor(SignedRequestHandler signedRequestHandler)
    {
        this.signedRequestHandler = signedRequestHandler;
    }

    public static SignedRequestHandler getSignedRequestHandler()
    {
        return signedRequestHandler;
    }
}
