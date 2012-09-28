package com.atlassian.labs.remoteapps.kit.common.spring.ns;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Namespace handler for all remote app spring elements
 */
public class RemoteAppsNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("bean-scan", new BeanScanBeanDefinitionParser());
    }
}
