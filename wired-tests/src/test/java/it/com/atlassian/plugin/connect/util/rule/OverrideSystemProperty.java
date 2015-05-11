package it.com.atlassian.plugin.connect.util.rule;

import com.google.common.base.Preconditions;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Override a system property for the duration of a test.
 */
public class OverrideSystemProperty implements TestRule
{
    private final String propertyName;
    private final String overrideValue;

    public OverrideSystemProperty(final String propertyName, final String overrideValue)
    {
        this.propertyName = Preconditions.checkNotNull(propertyName);
        this.overrideValue = overrideValue;
    }

    @Override
    public Statement apply(final Statement base, final Description description)
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                final String originalValue = System.getProperty(propertyName);
                if (overrideValue != null)
                {
                    System.setProperty(propertyName, overrideValue);
                }
                else
                {
                    System.clearProperty(propertyName);
                }
                try
                {
                    base.evaluate();
                }
                finally
                {
                    if (originalValue != null)
                    {
                        System.setProperty(propertyName, originalValue);
                    }
                    else
                    {
                        System.clearProperty(propertyName);
                    }
                }
            }
        };
    }
}
