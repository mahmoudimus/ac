package junit.confluence;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.xmlrpc.ServiceMethod;
import com.atlassian.xmlrpc.ServiceObject;

/**
 *
 */
@ServiceObject("confluence2")
public interface SpaceService
{
    @ServiceMethod("getSpace")
    Promise<Space> getSpace(String token, String spaceKey);
}
