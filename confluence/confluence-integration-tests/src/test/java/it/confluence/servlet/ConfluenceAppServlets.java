package it.confluence.servlet;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import com.atlassian.connect.test.confluence.pageobjects.RemoteMacroEditorDialog;
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.confluence.blueprint.BlueprintContextRequestTypeToken;
import com.atlassian.plugin.connect.test.common.servlet.BodyExtractor;
import com.atlassian.plugin.connect.test.common.servlet.HttpContextServlet;
import com.atlassian.plugin.connect.test.common.servlet.MustacheServlet;

import com.google.gson.Gson;

import static com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets.wrapContextAwareServlet;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Utility methods for creating test servlets suitable for serving Confluence-specific Connect iframes.
 */
public class ConfluenceAppServlets
{
    public static HttpServlet dynamicMacroStaticServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("it/confluence/macro/dynamic-macro-static.mu"));
    }

    public static HttpServlet macroEditor()
    {
        return wrapContextAwareServlet(new MustacheServlet(RemoteMacroEditorDialog.TEMPLATE_PATH));
    }

    public static HttpServlet macroBodyEditor(String newMacroBody)
    {
        HttpContextServlet contextServlet = new HttpContextServlet(new MustacheServlet("it/confluence/macro/editor-macro-body.mu"));
        contextServlet.getBaseContext().put("newMacroBody", newMacroBody);
        return contextServlet;
    }

    public static HttpServlet blueprintTemplateServlet()
    {
        return wrapContextAwareServlet(new MustacheServlet("it/confluence/macro/test-blueprint.xml"));
    }

    /**
     * @return a servlet that contains 3 buttons to navigate to different parts of confluence
     */
    public static HttpServlet navigatorServlet(Long id, String spaceKey)
    {
        HttpContextServlet contextServlet = new HttpContextServlet(new MustacheServlet("it/confluence/navigator/iframe-navigator.mu"));
        contextServlet.getBaseContext().put("contentId", id);
        contextServlet.getBaseContext().put("spaceKey", spaceKey);
        return contextServlet;
    }

    public static HttpServlet blueprintContextServlet()
    {
        return wrapContextAwareServlet(
                new MustacheServlet("it/confluence/blueprint/context.json", HttpMethod.POST),
                Collections.emptyList(),
                newArrayList(new JsonExtractor())
        );
    }

    private static class JsonExtractor implements BodyExtractor
    {
        private static final Gson GSON = new Gson();
        private static final Type requestType = new BlueprintContextRequestTypeToken().getType();

        @Override
        public Map<String, String> extractAll(String jsonString)
        {
            Map<String, String> json = GSON.fromJson(jsonString, requestType);
            return json;
        }
    }
}
