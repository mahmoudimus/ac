package com.atlassian.plugin.connect.spi.module;

@Deprecated
public interface CurrentUserProvider<T>
{

    Class<T> getUserType();

    T getCurrentUser();
}
