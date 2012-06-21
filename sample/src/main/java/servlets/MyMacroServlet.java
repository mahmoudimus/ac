package servlets;

import com.atlassian.labs.remoteapps.apputils.OAuthContext;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static services.HttpUtils.renderHtml;

/**
 *
 */
@Component
public class MyMacroServlet extends HttpServlet
{
    private final OAuthContext oauthContext;

    public MyMacroServlet(OAuthContext oauthContext)
    {
        this.oauthContext = oauthContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        oauthContext.validateRequest(req);
        final String pageId = req.getParameter("ctx_page_id");
        final String favoriteFooty = req.getParameter("footy");
        final String body = req.getParameter("body");
        resp.setDateHeader("Expires", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10));
        resp.setHeader("Cache-Control", "public");

        renderHtml(resp, "macro.mu", new HashMap<String,Object>() {{
                put("pageId", pageId);
                put("favoriteFooty", favoriteFooty);
                put("body", body);
                put("server", URI.create(oauthContext.getLocalBaseUrl()).getAuthority());
        }});
    }
}
