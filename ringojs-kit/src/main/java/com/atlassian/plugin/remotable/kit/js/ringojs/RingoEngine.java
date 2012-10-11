package com.atlassian.plugin.remotable.kit.js.ringojs;

import com.atlassian.plugin.remotable.kit.js.ringojs.js.AppContext;
import com.atlassian.plugin.remotable.kit.js.ringojs.repository.FileRepository;
import com.atlassian.plugin.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.ringojs.engine.RhinoEngine;
import org.ringojs.engine.RingoConfiguration;
import org.ringojs.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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
            ringoConfig.addModuleRepository(ringoHome);
            ringoConfig.addModuleRepository(home);

            // todo: add wrap factory to wrap things like futures when we get a promises library

            if (log.isDebugEnabled())
            {
                //ringoConfig.setDebug(true);
            }
            engine = new RhinoEngine(ringoConfig, new HashMap<String, Object>()
            {{
                put("appContext", new AppContext(appBundle));
            }});
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
