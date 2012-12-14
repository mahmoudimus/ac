package com.atlassian.plugin.remotable.sisu;

import com.google.inject.Binder;
import org.eclipse.sisu.binders.Wiring;
import org.eclipse.sisu.scanners.analyzer.WiringFactory;

public final class ComponentImportWiringFactory implements WiringFactory
{
    @Override
    public Wiring create(Binder binder)
    {
        return new ComponentImportWiring(binder);
    }
}
