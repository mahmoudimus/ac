package it.com.atlassian.plugin.connect.rule;

/**
 * Disable dev mode for the duration of a test.
 */
public class DisableDevMode extends OverrideSystemProperty
{
    public DisableDevMode()
    {
        super("atlassian.dev.mode", Boolean.FALSE.toString());
    }
}
