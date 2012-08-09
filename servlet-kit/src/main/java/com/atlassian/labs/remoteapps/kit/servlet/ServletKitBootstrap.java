package com.atlassian.labs.remoteapps.kit.servlet;

import com.atlassian.labs.remoteapps.api.HttpResourceMounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import java.util.Collection;
import java.util.Locale;

/**
 *
 */
public class ServletKitBootstrap
{
    private static final Logger log = LoggerFactory.getLogger(ServletKitBootstrap.class);

    public ServletKitBootstrap(ApplicationContext applicationContext,
            HttpResourceMounter httpResourceMounter
    ) throws Exception
    {
        for (HttpServlet servlet : (Collection<HttpServlet>)applicationContext.getBeansOfType(Servlet.class).values())
        {
            String path;
            AppUrl appUrl = servlet.getClass().getAnnotation(AppUrl.class);
            if (appUrl != null)
            {
                path = appUrl.value();
            }
            else
            {
                String className = servlet.getClass().getSimpleName();
                path = "/" + String.valueOf(className.charAt(0)).toLowerCase(Locale.US) +
                        (className.endsWith("Servlet") ? className.substring(1, className.length() - "Servlet".length()) : className.substring(1, className.length()));
            }
            log.info("Found servlet '" + path + "' class '" + servlet.getClass());
            httpResourceMounter.mountServlet(servlet, path, path + "/*");
        }

        httpResourceMounter.mountStaticResources("", "/public/*");
    }
}
