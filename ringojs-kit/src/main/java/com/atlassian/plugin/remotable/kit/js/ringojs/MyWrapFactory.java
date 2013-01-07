package com.atlassian.plugin.remotable.kit.js.ringojs;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.ringojs.engine.RingoWrapFactory;

public class MyWrapFactory extends RingoWrapFactory
{
    @Override
    public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class<?> staticType)
    {
        return new MyNativeJavaObject(scope, javaObject, staticType);
    }
}
