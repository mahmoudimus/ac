package com.atlassian.plugin.remotable.sisu;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;

final class BundleDisposer implements Disposer
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final LinkedList<Disposable> disposables = new LinkedList<Disposable>();

    public BundleDisposer(Bundle bundle)
    {
        bundle.getBundleContext().addBundleListener(new BundleListener()
        {
            @Override
            public void bundleChanged(BundleEvent event)
            {
                if (event.getType() == BundleEvent.STOPPING)
                {
                    disposeAll();
                }
            }
        });
    }

    private void disposeAll()
    {
        final Iterator<Disposable> disposablesIterator = new LinkedList<Disposable>(disposables).descendingIterator();
        while (disposablesIterator.hasNext())
        {
            final Disposable d = disposablesIterator.next();
            try
            {
                d.dispose();
            }
            catch (DisposeException e)
            {
                logger.warn("Could not dispose cleanly of {}, calling {}", d.object, d.method);
                logger.warn("Here is what happened:", e);
            }
        }
    }

    @Override
    public void register(Method method, Object object)
    {
        disposables.add(new Disposable(method, object));
    }
}
