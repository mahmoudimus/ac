package servlets;

import ao.model.SampleEntity;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.labs.remoteapps.api.annotation.ServiceReference;
import com.atlassian.labs.remoteapps.api.service.SignedRequestHandler;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.google.common.base.Preconditions.*;

//@AppUrl("/ao")
@Singleton
public final class ActiveObjectsServlet extends HttpServlet
{
    private final SignedRequestHandler signedRequestHandler;
    private final ActiveObjects ao;

    public ActiveObjectsServlet(@ServiceReference SignedRequestHandler signedRequestHandler, @ServiceReference ActiveObjects ao)
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
                        "        <link rel=\"stylesheet\" type=\"text/css\" href=\"%1$s/remoteapps/all.css\">" +
                        "        <script src=\"%1$s/remoteapps/all.js\"></script>\n" +
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
