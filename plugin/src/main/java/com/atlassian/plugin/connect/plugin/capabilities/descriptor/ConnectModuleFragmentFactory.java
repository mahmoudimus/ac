package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import org.dom4j.dom.DOMElement;

/**
 * @since 1.0
 */
public interface ConnectModuleFragmentFactory<T>
{
    DOMElement createFragment(String pluginKey, T bean);
}
