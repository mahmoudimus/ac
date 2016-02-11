package it.confluence.servlet;

import com.atlassian.connect.test.confluence.pageobjects.RemoteMacroEditorDialog;
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintContextPostBody;
import com.atlassian.plugin.connect.test.common.servlet.BodyExtractor;
import com.atlassian.plugin.connect.test.common.servlet.ErrorServlet;
import com.atlassian.plugin.connect.test.common.servlet.HttpContextServlet;
import com.atlassian.plugin.connect.test.common.servlet.MustacheServlet;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

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

    public static HttpServlet macroPropertyPanel()
    {
        return wrapContextAwareServlet(new MustacheServlet("it/confluence/macro/property-panel.mu"));
    }

    public static HttpServlet macroPropertyPanelWithDialog()
    {
        return wrapContextAwareServlet(new MustacheServlet("it/confluence/macro/property-panel-dialog.mu"));
    }

    public static HttpServlet macroBodyEditor(String newMacroBody)
    {
        HttpContextServlet contextServlet = new HttpContextServlet(new MustacheServlet("it/confluence/macro/editor-macro-body.mu"));
        contextServlet.getBaseContext().put("newMacroBody", newMacroBody);
        return contextServlet;
    }

    public static HttpServlet blueprintTemplateServlet(final String templatePath)
    {
        MustacheServlet mustacheServlet = new MustacheServlet(templatePath, "application/xml");
        return wrapContextAwareServlet(mustacheServlet);
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

    public static HttpServlet blueprintMalformedContextServlet()
    {
        return wrapContextAwareServlet(
                new MustacheServlet("it/confluence/blueprint/contextMalformed.json.txt", HttpMethod.POST),
                Collections.emptyList(),
                newArrayList(new JsonExtractor())
        );
    }

    public static HttpServlet blueprint404Servlet()
    {
        return wrapContextAwareServlet(
                new ErrorServlet(HttpServletResponse.SC_NOT_FOUND, HttpMethod.POST),
                Collections.emptyList(),
                newArrayList(new JsonExtractor())
        );
    }

    private static class JsonExtractor implements BodyExtractor
    {
        private static final Gson GSON = new Gson();

        @Override
        public Map<String, String> extractAll(String jsonString)
        {
            BlueprintContextPostBody postBody = GSON.fromJson(jsonString, BlueprintContextPostBody.class);
            return ImmutableMap.of(
                    "addonKey", postBody.getAddonKey(),
                    "blueprintKey", postBody.getBlueprintKey(),
                    "spaceKey", postBody.getSpaceKey(),
                    "userKey", postBody.getUserKey(),
                    "userLocale", postBody.getUserLocale().toString()
            );
        }
    }
}
