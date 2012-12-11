package com.atlassian.plugin.remotable.sisu;

import java.lang.reflect.Method;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.2
 */
public interface Disposer
{
    void register(Method method, Object injectee);
}
