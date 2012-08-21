package com.atlassian.labs.remoteapps.kit.js.ringojs.repository;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.provider.StrongCachingModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Copyright 2011 Mark Derricutt.
 * <p/>
 * Contributing authors:
 * Daniel Bower
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * <p/>
 * Wrapper around the coffee-script compiler from https://github.com/jashkenas/coffee-script/
 *
 * We run the compilation in a different thread as you can't create a new context when one is already
 * associated with the thread.  This is a by-design Rhino limitation.
 */
public class CoffeeScriptCompiler
{

    private boolean bare;
    private String version;
    private final Scriptable globalScope;
    private Scriptable coffeeScript;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CoffeeScriptCompiler(String version, boolean bare) {
        this.bare = bare;
        this.version = version;

        try {
            Context context = createContext();
            globalScope = context.initStandardObjects();
            final Require require = getSandboxedRequire(context, globalScope, true);
            coffeeScript = require.requireMain(context, "coffee-script");
        } catch (Exception e1) {
            throw new CoffeeScriptException("Unable to load the coffeeScript compiler into Rhino", e1);
        } finally {
            Context.exit();
        }

    }

    public String compile(final String coffeeScriptSource)
    {
        try
        {
            return executor.submit(new Callable<String>()
            {
                @Override
                public String call() throws Exception
                {
                    return doCompile(coffeeScriptSource);
                }
            }).get();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException(e);
        }
    }
    private String doCompile(final String coffeeScriptSource) {
        Context context = createContext();
        try
        {
            Scriptable compileScope = context.newObject(coffeeScript);
            compileScope.setParentScope(coffeeScript);
            compileScope.put("coffeeScript", compileScope, coffeeScriptSource);
            try
            {

                String options = bare ? "{bare: true}" : "{}";

                return (String) context.evaluateString(
                        compileScope,
                        String.format("compile(coffeeScript, %s);", options),
                        "source", 0, null);
            }
            catch (JavaScriptException e)
            {
                throw new CoffeeScriptException(e.getMessage(), e);
            }
        }
        finally
        {
            Context.exit();
        }
    }

    private Context createContext() {
        Context context = Context.enter();
        context.setOptimizationLevel(-1); // Without this, Rhino hits a 64K bytecode limit and fails
        return context;
    }

    private Require getSandboxedRequire(Context cx, Scriptable scope, boolean sandboxed) throws URISyntaxException {
        return new Require(cx, cx.initStandardObjects(),
                new StrongCachingModuleScriptProvider(
                        new UrlModuleSourceProvider(Collections.singleton(
                                getDirectory()), null)), null, null, sandboxed);
    }

    private URI getDirectory() throws URISyntaxException {
        final String resourcePath = String.format("/modules/coffee-script-%s/", version);
        return getClass().getResource(resourcePath).toURI();
    }

}
