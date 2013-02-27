package com.atlassian.plugin.remotable.sisu;

import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;

import javax.annotation.PreDestroy;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;

final class PreDestroyAbstractAnnotatedMethodTypeListener extends AbstractAnnotatedMethodTypeListener
{
    private final Disposer disposer;

    public PreDestroyAbstractAnnotatedMethodTypeListener(Disposer disposer)
    {
        super(PreDestroy.class);
        this.disposer = checkNotNull(disposer);
    }

    @Override
    protected <I> void hear(final Method method, TypeEncounter<I> encounter)
    {
        encounter.register(new InjectionListener<I>()
        {
            public void afterInjection(I injectee)
            {
                disposer.register(method, injectee);
            }
        });
    }
}
