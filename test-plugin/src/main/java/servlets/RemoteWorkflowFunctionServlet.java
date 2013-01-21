package servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Named
public class RemoteWorkflowFunctionServlet extends HttpServlet
{

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException
    {
        super.doPost(req, resp);
        System.out.println("Remote workflow post function fired: " + req.getReader().readLine());
    }
}
