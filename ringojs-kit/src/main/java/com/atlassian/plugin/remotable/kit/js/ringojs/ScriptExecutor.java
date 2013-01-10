package com.atlassian.plugin.remotable.kit.js.ringojs;

import com.google.common.base.Function;
import org.mozilla.javascript.Context;

public interface ScriptExecutor
{
    public Object execute(Function<Context, Object> f);
}
