package com.atlassian.plugin.connect.plugin.web;

import com.atlassian.fugue.Option;

/**
 * List of phases, to which plugins/products implementing Connect SPI can plug their filters.
 */
public enum ConnectRequestFilterPhase
{
    /**
     * Request filtering happens after successful API scope check.
     */
    AFTER_API_SCOPING_FILTER("after-api-scoping-filter");

    private final String phaseName;

    ConnectRequestFilterPhase(final String phaseName)
    {
        this.phaseName = phaseName;
    }

    public static Option<ConnectRequestFilterPhase> getConnectRequestFilterPhase(final String phaseName)
    {
        for (ConnectRequestFilterPhase phase : values())
        {
            if (phase.phaseName.equals(phaseName))
            {
                return Option.some(phase);
            }
        }
        return Option.none();
    }
}
