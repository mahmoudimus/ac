package com.atlassian.plugin.connect.api.util;

/**
 * @since 1.0
 */
public interface ConnectContainerUtil
{
    <T> T createBean(Class<T> clazz);

    <T> Iterable<T> getBeansOfType(Class<T> clazz);
}
