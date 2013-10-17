package com.atlassian.plugin.connect.plugin.capabilities.util;

/**
 * @since version
 */
public interface ConnectAutowireUtil
{
    <T> T createBean(Class<T> clazz);
}
