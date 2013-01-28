package com.atlassian.plugin.remotable.kit.js.ringojs;

import com.atlassian.plugin.remotable.kit.js.ringojs.js.AppContext;
import com.atlassian.plugin.remotable.kit.js.ringojs.repository.BundleRepository;
import com.atlassian.plugin.remotable.kit.js.ringojs.repository.FileRepository;
import com.atlassian.plugin.Plugin;
import com.google.common.base.Function;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaMethod;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.ringojs.engine.RhinoEngine;
import org.ringojs.engine.RingoConfiguration;
import org.ringojs.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

/**
 *
 */
public class RingoEngine
{
    private final RhinoEngine engine;
    private static final Logger log = LoggerFactory.getLogger(RingoEngine.class);

    public RingoEngine(Plugin plugin, final BundleContext bundleContext)
    {
        try
        {
            Field field = NativeJavaMethod.class.getDeclaredField("debug");
            Field modifiersField = field.getClass().getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.setAccessible(true);
            field.setBoolean(null, true);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }

        Repository home = null;
        final Bundle appBundle = bundleContext.getBundle();
        Repository ringoHome = new BundleRepository(appBundle, "/modules");

        URL baseUrl = plugin.getResource("/");
        if ("file".equals(baseUrl.getProtocol()))
        {
            try
            {
                File baseDir = new File(baseUrl.toURI());
                home = new FileRepository(baseDir);
            }
            catch (URISyntaxException e)
            {
                throw new RuntimeException("Invalid base url: " + baseUrl, e);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Invalid base dir: " + baseUrl, e);
            }
        }
        else
        {
            home = new BundleRepository(appBundle, "/");
        }
        try
        {
            RingoConfiguration ringoConfig = new RingoConfiguration(ringoHome, null, null);
            MyWrapFactory myWrapFactory = new MyWrapFactory();
            ringoConfig.setWrapFactory(myWrapFactory);
            ringoConfig.addModuleRepository(ringoHome);
            ringoConfig.addModuleRepository(home);

            if (log.isDebugEnabled())
            {
                //ringoConfig.setDebug(true);
            }
            engine = new RhinoEngine(ringoConfig, new HashMap<String, Object>()
            {{
                put("appContext", new AppContext(appBundle));
            }});

            myWrapFactory.setExecutor(new ScriptExecutor()
            {
                @Override
                public Object execute(Function<Context, Object> f)
                {
                    Context context = engine.getContextFactory().enterContext();
                    try
                    {
                        return f.apply(context);
                    }
                    finally
                    {
                        Context.exit();
                    }
                }
            });
        }
        catch (Exception x)
        {
            throw new RuntimeException(x);
        }
    }

    public RhinoEngine getEngine()
    {
        return engine;
    }
}
