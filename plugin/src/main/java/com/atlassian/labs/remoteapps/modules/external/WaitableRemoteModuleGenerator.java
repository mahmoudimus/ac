package com.atlassian.labs.remoteapps.modules.external;

import org.dom4j.Element;

/**
 * A remote module generator that needs to wait for some condition before loading
 */
public interface WaitableRemoteModuleGenerator extends RemoteModuleGenerator
{
    void waitToLoad(Element element);
}
