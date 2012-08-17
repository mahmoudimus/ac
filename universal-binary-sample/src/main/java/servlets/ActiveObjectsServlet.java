package servlets;

import ao.model.SampleEntity;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.labs.remoteapps.api.annotation.ServiceReference;
import com.atlassian.labs.remoteapps.api.services.SignedRequestHandler;
import com.atlassian.labs.remoteapps.kit.servlet.AppUrl;
import com.google.common.collect.ImmutableMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@AppUrl("/ao")
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
        try
        {
            final String consumerKey = signedRequestHandler.validateRequest(req);

            final SampleEntity e = ao.create(SampleEntity.class);
            final String textParameter = req.getParameter("text");
            e.setText(textParameter != null ? textParameter : "AO is working!");
            e.save();

            final Map<String, Object> context = ImmutableMap.<String, Object>of(
                    "baseUrl", signedRequestHandler.getHostBaseUrl(consumerKey),
                    "savedText", e.getText());

//            renderHtml(resp, "test-ao.mu", context);
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }
}
