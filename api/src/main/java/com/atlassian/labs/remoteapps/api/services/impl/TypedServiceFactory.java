package com.atlassian.labs.remoteapps.api.services.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;

public interface TypedServiceFactory<T> extends ServiceFactory
{
    public T getService(Bundle bundle);
}
