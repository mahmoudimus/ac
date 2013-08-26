package com.atlassian.plugin.connect;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.Condition;
import com.atlassian.plugin.connect.test.server.module.DialogPageModule;
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
            AtlassianConnectAddOnRunner runner = new AtlassianConnectAddOnRunner("http://localhost:2990/jira", "permanentRedirect")
                    .addOAuth()
                    //.enableLicensing()
                    .addPermission("resttest")
                    .add(GeneralPageModule.key("remotePluginGeneral")
                                          .name("Remotable Plugin app1 General")
                                          .path("/rpg")
                                          .linkName("Remotable Plugin app1 General Link")
                                          .iconUrl("/public/sandcastles.jpg")
                                          .height("600")
                                          .width("700")
                                          .resource(newMustacheServlet("iframe.mu")))
                    .add(GeneralPageModule.key("amdTest")
                                          .name("AMD Test app1 General")
                                          .path("/amdTest")
                                          .resource(newMustacheServlet("amd-test.mu")))
                    .add(DialogPageModule.key("remotePluginDialog")
                                         .name("Remotable Plugin app1 Dialog")
                                         .path("/rpd")
                                         .resource(newMustacheServlet("dialog.mu")))
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
