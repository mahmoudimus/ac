package com.atlassian.plugin.connect;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.GeneralPageModule;

import static com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner.newMustacheServlet;

/**
 * @since version
 */
public class AppRunner
{
    public static void main(String[] args)
    {
        try
        {
//            AtlassianConnectAddOnRunner pluginFirst = new AtlassianConnectAddOnRunner("http://localhost:2990/jira", "pluginFirst")
//                    .add(GeneralPageModule.key("changedPage")
//                                          .name("Changed Page")
//                                          .path("/page")
//                                          .resource(newMustacheServlet("hello-world-page.mu")))
//                    .start();

            AtlassianConnectAddOnRunner runner = new AtlassianConnectAddOnRunner("http://localhost:2990/jira", "permanentRedirect")
                    .add(GeneralPageModule.key("page")
                                          .name("Page")
                                          .path("/page")
                                          .resource(new MessageServlet()))
                    .start();
            
            while (true)
            {
                //do nothing
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static final class MessageServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            resp.setContentType("text/plain");
            resp.getWriter().write(req.getParameter("message"));
            resp.getWriter().close();
        }
    }
}
