package it.servlet.macro;

import com.atlassian.plugin.connect.test.HttpUtils;
import com.google.common.collect.ImmutableMap;
import it.servlet.ContextServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;
import static it.confluence.ContextParameters.*;

public class SimpleMacroServlet extends ContextServlet
{
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        doExecute(req, resp, context);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws ServletException, IOException
    {
        doExecute(req, resp, context);
    }

    private void doExecute(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> context) throws IOException
    {
        HttpUtils.renderHtml(resp, "confluence/macro/simple.mu", getContext(req, context));
    }

    private Map<String, Object> getContext(HttpServletRequest req, Map<String, Object> context)
    {
        return ImmutableMap.<String, Object>builder()
                           .putAll(context)
                           .put(CTX_OUTPUT_TYPE, getParam(req, CTX_OUTPUT_TYPE))
                           .put(CTX_PAGE_ID, getParam(req, CTX_PAGE_ID))
                           .put(CTX_PAGE_TYPE, getParam(req, CTX_PAGE_TYPE))
                           .put(CTX_PAGE_TITLE, getParam(req, CTX_PAGE_TITLE))
                           .put(CTX_SPACE_KEY, getParam(req, CTX_SPACE_KEY))
                           .put(CTX_USER_ID, getParam(req, CTX_USER_ID))
                           .put(CTX_USER_KEY, getParam(req, CTX_USER_KEY))
                           .build();
    }

    protected String getParam(HttpServletRequest req, String name)
    {
        return nullToEmpty(req.getParameter("ctx_" + name));
    }
}
