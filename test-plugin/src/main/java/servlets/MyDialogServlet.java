package servlets;

import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.atlassian.plugin.remotable.api.service.RequestContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static services.HttpUtils.renderHtml;

@Named
public class MyDialogServlet extends HttpServlet
{
    private final RequestContext requestContext;

    @Inject
    public MyDialogServlet(@ComponentImport RequestContext requestContext)
    {
        this.requestContext = requestContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final Map<String, Object> context = new HashMap<String,Object>();
        context.put("baseUrl", requestContext.getHostBaseUrl());
        renderHtml(resp, "test-dialog.mu", context);
    }
}
