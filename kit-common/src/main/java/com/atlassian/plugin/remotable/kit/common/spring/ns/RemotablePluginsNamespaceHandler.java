package com.atlassian.plugin.remotable.kit.common.spring.ns;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Namespace handler for all remote plugin spring elements
 */
public class RemotablePluginsNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("bean-scan", new BeanScanBeanDefinitionParser());
    }
}
