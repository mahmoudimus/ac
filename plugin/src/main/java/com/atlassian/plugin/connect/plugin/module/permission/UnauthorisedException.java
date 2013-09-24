package com.atlassian.plugin.connect.plugin.module.permission;

import static com.google.common.base.Preconditions.checkNotNull;

public class UnauthorisedException extends Exception
{
    /**
     * Our Applications may prefer to return 404's to caller rather than 401
     */
    public enum HandlingPolicy { RETURN_404, RETURN_401 }

    private final HandlingPolicy handlingPolicy;

    public UnauthorisedException(HandlingPolicy handlingPolicy, String message)
    {
        super(message);
        this.handlingPolicy = checkNotNull(handlingPolicy);
    }

    public HandlingPolicy getHandlingPolicy()
    {
        return handlingPolicy;
    }

}
