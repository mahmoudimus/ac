package com.atlassian.labs.remoteapps.modules;

import org.dom4j.Element;

import java.util.Set;

/**
 * A remote module generator
 */
public interface RemoteModuleGenerator
{
    String getType();

    Set<String> getDynamicModuleTypeDependencies();

    RemoteModule generate(RemoteAppCreationContext ctx, Element element);

}
