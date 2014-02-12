package com.atlassian.plugin.connect.plugin.capabilities.util;

/**
 * @since 1.0
 */
public interface ConnectContainerUtil
{
    <T> T createBean(Class<T> clazz);

    <T> Iterable<T> getBeansOfType(Class<T> clazz);
}
