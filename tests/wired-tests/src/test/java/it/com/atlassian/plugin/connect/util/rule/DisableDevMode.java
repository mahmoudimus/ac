package it.com.atlassian.plugin.connect.util.rule;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Disable dev mode for the duration of a test.
 */
public class DisableDevMode extends OverrideSystemProperty
{
    public DisableDevMode()
    {
        super("atlassian.dev.mode", Boolean.FALSE.toString());
    }

    @Override
    public Statement apply(Statement base, Description description)
    {
        if (description.getAnnotation(DevMode.class) != null)
        {
            return base;
        }
        return super.apply(base, description);
    }
}
