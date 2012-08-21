package services;


import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SignedRequestHandlerAccessor
{
    private static SignedRequestHandler signedRequestHandler;

    @Inject
    public SignedRequestHandlerAccessor(SignedRequestHandler signedRequestHandler)
    {
        this.signedRequestHandler = signedRequestHandler;
    }

    public static SignedRequestHandler getSignedRequestHandler()
    {
        return signedRequestHandler;
    }
}
