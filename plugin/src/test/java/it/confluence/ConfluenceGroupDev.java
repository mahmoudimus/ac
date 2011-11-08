package it.confluence;

import it.AbstractGroupClient;
import org.junit.Ignore;

/**
 * Only runs confluence tests, used for development
 */
public class ConfluenceGroupDev extends AbstractGroupClient
{
    public ConfluenceGroupDev()
    {
        super("confluence", 1990, "/confluence");
    }
}
