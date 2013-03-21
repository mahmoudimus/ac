package servlets;

import com.atlassian.pluginkit.servlet.AbstractPageServlet;
import com.atlassian.pluginkit.servlet.AppScripts;

import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Named
@AppScripts({ "jquery-1.7", "amd-test" })
public class AmdTestServlet extends AbstractPageServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        render(req, res, Collections.<String,Object>emptyMap());
    }
}
