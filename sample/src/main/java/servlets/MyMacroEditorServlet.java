package servlets;

import com.atlassian.labs.remoteapps.api.annotation.ServiceReference;
import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static services.HttpUtils.renderHtml;

/**
 *
 */
@Singleton
public class MyMacroEditorServlet extends HttpServlet
{
    private final SignedRequestHandler signedRequestHandler;

    @Inject
    public MyMacroEditorServlet(@ServiceReference SignedRequestHandler signedRequestHandler)
    {
        this.signedRequestHandler = signedRequestHandler;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String consumerKey = signedRequestHandler.validateRequest(req);
        final Map<String, Object> context = new HashMap<String,Object>();
        context.put("baseUrl", signedRequestHandler.getHostBaseUrl(consumerKey));
        renderHtml(resp, "macro-editor.mu", context);
    }
}
