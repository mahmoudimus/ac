package it.servlet.condition;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;

/**
 *
 */
public class ToggleableConditionServlet extends HttpServlet
{
    public static final String TOGGLE_CONDITION_URL = "/toggleableCondition";

    public static ConditionalBean toggleableConditionBean()
    {
        return newSingleConditionBean().withCondition(TOGGLE_CONDITION_URL).build();
    }

    private final AtomicBoolean shouldDisplay;
    private final boolean initialValue;

    public ToggleableConditionServlet(boolean initialValue)
    {
        this.initialValue = initialValue;
        this.shouldDisplay = new AtomicBoolean(initialValue);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("application/json");
        resp.getWriter().write("{\"shouldDisplay\" : " + shouldDisplay.get() + "}");
        resp.getWriter().close();
    }

    public void setShouldDisplay(boolean shouldDisplay)
    {
        this.shouldDisplay.set(shouldDisplay);
    }

    /**
     * @return a {@link org.junit.rules.TestRule} that reverts the condition back to it's initial value.
     */
    public TestRule resetToInitialValueRule()
    {
        return new TestRule()
        {
            @Override
            public Statement apply(final Statement base, final Description description)
            {
                return new Statement()
                {
                    @Override
                    public void evaluate() throws Throwable
                    {
                        try
                        {
                            base.evaluate();
                        }
                        finally
                        {
                            setShouldDisplay(initialValue);
                        }
                    }
                };
            }
        };
    }

}
