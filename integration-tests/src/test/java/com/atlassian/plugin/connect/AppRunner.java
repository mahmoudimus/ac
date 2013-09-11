package com.atlassian.plugin.connect;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.*;

import it.MyContextAwareWebPanelServlet;
import it.confluence.TestConfluencePageMacro;

import static com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner.newMustacheServlet;
import static com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner.newServlet;

/**
 * @since version
 */
public class AppRunner
{
    /**
    * Hello Summit!
    */

    public static void main(String[] args)
    {
        try
        {
//            AtlassianConnectAddOnRunner runner = new AtlassianConnectAddOnRunner("http://localhost:1990/confluence", "permanentRedirect")
//                    .addOAuth()
//                    //.enableLicensing()
//                    .addPermission("resttest")
//                    .add(GeneralPageModule.key("remotePluginGeneral")
//                                          .name("Remotable Plugin app1 General")
//                                          .path("/rpg")
//                                          .linkName("Remotable Plugin app1 General Link")
//                                          .iconUrl("/public/sandcastles.jpg")
//                                          .height("600")
//                                          .width("700")
//                                          .resource(newMustacheServlet("iframe.mu")))
//                    .add(GeneralPageModule.key("amdTest")
//                                          .name("AMD Test app1 General")
//                                          .path("/amdTest")
//                                          .resource(newMustacheServlet("amd-test.mu")))
//                    .add(DialogPageModule.key("remotePluginDialog")
//                                         .name("Remotable Plugin app1 Dialog")
//                                         .path("/rpd")
//                                         .resource(newMustacheServlet("dialog.mu")))
//                    .start();

            AtlassianConnectAddOnRunner remotePlugin = new AtlassianConnectAddOnRunner("http://localhost:1990/confluence")
                    .addOAuth()
                    .addPermission("read_content")
                    .addPermission("read_users_and_groups")
                    .addPermission("read_server_information")
                    .add(RemoteMacroModule.key("app1-macro")
                                          .name("app1-macro")
                                          .title("Remotable Plugin app1 Macro")
                                          .path("/app1-macro")
                                          .iconUrl("/public/sandcastles.jpg")
                                          .outputBlock()
                                          .bodyType("rich-text")
                                          .featured("true")
                                          .category(MacroCategory.name("development"))
                                          .parameters(MacroParameter.name("footy").title("Favorite Footy").type("enum").required("true").values("American Football", "Soccer", "Rugby Union", "Rugby League"))
                                          .contextParameters(ContextParameter.name("page.id").query())
                                          .editor(MacroEditor.at("/myMacroEditor").height("600").width("600").resource(newMustacheServlet("confluence/macro/editor.mu")))
                                          .resource(new TestConfluencePageMacro.MyMacroServlet()))
                    .add(GeneralPageModule.key("remotePluginGeneral")
                                          .name("Remotable Plugin app1 General")
                                          .path("/page?page_id=${page.id}")
                                          .linkName("Remotable Plugin app1 General Link")
                                          .iconUrl("/public/sandcastles.jpg")
                                          .height("600")
                                          .width("700")
                                          .resource(newServlet(new MyContextAwareWebPanelServlet())))
                    .addRoute("/page/*", newServlet(new MyContextAwareWebPanelServlet()))
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
