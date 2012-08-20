package com.atlassian.labs.remoteapps.host.common.service;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;

public interface TypedServiceFactory<T> extends ServiceFactory
{
    public T getService(Bundle bundle);
}
