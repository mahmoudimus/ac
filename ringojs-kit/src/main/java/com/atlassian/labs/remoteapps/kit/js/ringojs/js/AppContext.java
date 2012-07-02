package com.atlassian.labs.remoteapps.kit.js.ringojs.js;

import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;

/**
 *
 */
public class AppContext
{
    private final Bundle appBundle;
    private final ServiceTracker beanFactoryTracker;
    private static Logger log = LoggerFactory.getLogger(AppContext.class);

    public AppContext(Bundle appBundle)
    {
        this.appBundle = appBundle;

        BundleContext bundleContext = appBundle.getBundleContext();
        Filter filter;
        try
        {
            filter = bundleContext.createFilter("(&(" + Constants.OBJECTCLASS +
                    "=org.springframework.beans.factory.BeanFactory)(Bundle-SymbolicName=" +
                    bundleContext.getBundle().getSymbolicName() + "))");
        }
        catch (InvalidSyntaxException e)
        {
            throw new RuntimeException(e);
        }
        beanFactoryTracker = new ServiceTracker(bundleContext, filter, null);
        beanFactoryTracker.open();
    }

    public Bundle getAppBundle()
    {
        return appBundle;
    }

    public Object getBean(String beanName)
    {
        BeanFactory beanFactory = (BeanFactory) beanFactoryTracker.getService();
        return beanFactory.getBean(beanName);
    }

    public Object getHostComponent(String name) {
        BundleContext bundleContext = appBundle.getBundleContext();
        try {
            String nameWithWildcard = name.replace('_', '*');
            ServiceReference[] refs = bundleContext.getServiceReferences(null, "(&(bean-name=" + nameWithWildcard + ")(plugins-host=true))");
            if (refs != null) {
                if (refs.length > 1) {
                    log.warn("More than one match for bean " + name + ", returning first");
                }
                return bundleContext.getService(refs[0]);
            }
        }
        catch (InvalidSyntaxException e) {
            log.error("Invalid syntax when searching for service with bean name of " + name, e);
        }
        return null;
    }

    public Object getService(String className) {
        BundleContext bundleContext = appBundle.getBundleContext();
        try {
            ServiceReference[] refs = bundleContext.getServiceReferences(className, null);
            if (refs != null) {
                if (refs.length > 1) {
                    log.warn("More than one match for service class  " + className + ", returning first");
                }
                return bundleContext.getService(refs[0]);
            }
        }
        catch (InvalidSyntaxException e) {
            log.error("Invalid syntax when searching for service with class name of " + className, e); 
        }
        return null;
    }

}
