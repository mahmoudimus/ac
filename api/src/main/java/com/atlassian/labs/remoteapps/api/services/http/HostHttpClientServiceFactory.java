package com.atlassian.labs.remoteapps.api.services.http;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;

public interface HostHttpClientServiceFactory extends ServiceFactory
{
    HostHttpClient getService(Bundle bundle);

}
