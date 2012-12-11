package com.atlassian.plugin.remotable.sisu;

import com.google.inject.Module;
import org.eclipse.sisu.scanners.module.ModuleFactory;
import org.osgi.framework.Bundle;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.2
 */
public class RemotableModuleFactory implements ModuleFactory
{
    @Override
    public Module getModule(Bundle bundle)
    {
        return new RemotableModule(bundle);
    }
}
