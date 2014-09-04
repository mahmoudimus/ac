package it.servlet.macro;

import com.google.common.collect.ImmutableMap;
import it.servlet.ContextServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.atlassian.plugin.connect.test.HttpUtils.renderHtml;
import static com.google.common.base.Strings.nullToEmpty;

public final class ExtendedMacroServlet extends ContextServlet
{
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        resp.setDateHeader("Expires", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10));
        resp.setHeader("Cache-Control", "public");

        final Map<String, Object> newContext = ImmutableMap.<String, Object>builder()
                                                           .putAll(context)
                                                           .put("footy", nullToEmpty(req.getParameter("footy")))
                                                           .put("body", nullToEmpty(req.getParameter("body")))
                                                           .build();

        renderHtml(resp, "confluence/macro/extended.mu", newContext);
    }
}
