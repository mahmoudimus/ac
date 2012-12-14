package servlets;

import ao.model.SampleEntity;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.remotable.api.annotation.ComponentImport;
import com.atlassian.plugin.remotable.api.service.SignedRequestHandler;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.google.common.base.Preconditions.*;

//@AppUrl("/ao")
@Named
public final class ActiveObjectsServlet extends HttpServlet
{
    private final SignedRequestHandler signedRequestHandler;
    private final ActiveObjects ao;

    @Inject
    public ActiveObjectsServlet(@ComponentImport SignedRequestHandler signedRequestHandler, @ComponentImport ActiveObjects ao)
    {
        this.signedRequestHandler = checkNotNull(signedRequestHandler);
        this.ao = checkNotNull(ao);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final String consumerKey = signedRequestHandler.validateRequest(req);
        final String hostBaseUrl = signedRequestHandler.getHostBaseUrl(consumerKey);

        try
        {
            final SampleEntity e = ao.create(SampleEntity.class);
            final String textParameter = req.getParameter("text");
            e.setText(textParameter != null ? textParameter : "AO is working!");
            e.save();

            renderHtml(resp, hostBaseUrl, "AO Success", "Woohoo!");
        }
        catch (Exception e)
        {
            renderHtml(resp, hostBaseUrl, "AO Error", e.getMessage());
        }
    }

    private void renderHtml(HttpServletResponse resp, String... strings) throws IOException
    {
        resp.getWriter().printf(
                "<!doctype html>\n" +
                        "<html>\n" +
                        "    <head>\n" +
                        "        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">\n" +
                        "        <link rel=\"stylesheet\" type=\"text/css\" href=\"%1$s/remotable-plugins/all.css\">" +
                        "        <script src=\"%1$s/remotable-plugins/all.js\"></script>\n" +
                        "        <script>RA.init();</script>\n" +
                        "        <title>%2$s</title>\n" +
                        "    </head>\n" +
                        "    <body>\n" +
                        "        <h1 id=\"message\">%2$s</h1>\n" +
                        "        <p>%3$s</p>\n" +
                        "    </body>\n" +
                        "</html>\n",
                strings);
        resp.getWriter().close();
    }
}
