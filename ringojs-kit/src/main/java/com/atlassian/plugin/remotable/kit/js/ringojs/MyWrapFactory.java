package com.atlassian.plugin.remotable.kit.js.ringojs;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.ringojs.engine.RingoWrapFactory;

import java.util.List;

public class MyWrapFactory extends RingoWrapFactory
{
    private ScriptExecutor executor;

    @Override
    public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class<?> staticType)
    {
        return new MyNativeJavaObject(scope, javaObject, staticType, executor);
    }

    public void setExecutor(ScriptExecutor executor)
    {
        this.executor = executor;
    }

    public Object wrap(Context cx, Scriptable scope, Object obj, Class staticType)
    {
        if (obj instanceof List)
        {
            return new MyScriptableList(scope, (List) obj);
        }
        else if (obj instanceof JavaScriptException)
        {
            JavaScriptException jse = (JavaScriptException) obj;
            Object nativeError = jse.getValue();
            if (nativeError != null)
            {
                return nativeError;
            }
        }
        return super.wrap(cx, scope, obj, staticType);
    }
}
