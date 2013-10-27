package com.atlassian.plugin.connect.plugin.capabilities.util;

/**
 * @since 1.0
 */
public interface ConnectAutowireUtil
{
    <T> T createBean(Class<T> clazz);
}
