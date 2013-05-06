package com.atlassian.plugin.remotable.plugin.util.node;

/**
 */
public interface ElementWriter<T extends ElementWriter>
{
    T copyText();

    T copyDescription();

    T copy(String propertyName);

    T copyIfExists(String propertyName);
}
