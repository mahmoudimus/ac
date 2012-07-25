package com.atlassian.labs.remoteapps.container.ao;

import com.atlassian.activeobjects.spi.TransactionSynchronisationManager;

public final class RemoteAppsTransactionSynchronisationManager implements TransactionSynchronisationManager
{
    @Override
    public boolean runOnRollBack(Runnable callback)
    {
        return false;
    }

    @Override
    public boolean runOnSuccessfulCommit(Runnable callback)
    {
        return false;
    }
}
