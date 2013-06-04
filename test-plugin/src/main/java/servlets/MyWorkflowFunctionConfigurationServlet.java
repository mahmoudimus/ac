package servlets;

import com.atlassian.pluginkit.servlet.AbstractPageServlet;
import com.atlassian.pluginkit.servlet.AppScripts;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Named
@AppScripts({"jquery-1.7", "my-workflow-function-configuration"})
public class MyWorkflowFunctionConfigurationServlet extends AbstractPageServlet
{

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException
    {
        render(req, res, ImmutableMap.<String, String>of());
    }
}
