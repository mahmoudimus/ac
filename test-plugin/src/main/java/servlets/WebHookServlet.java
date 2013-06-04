package servlets;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static util.JsonUtils.parseObject;

/**
 * Receives and stores web hooks
 */
@Named
public class WebHookServlet extends HttpServlet
{
    private final List<Publication> publications = new CopyOnWriteArrayList<Publication>();

    @Override
    protected synchronized void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String eventIdentifier = req.getPathInfo().substring(req.getPathInfo().lastIndexOf('/') + 1);
        StringBuilder body = new StringBuilder();
        char[] buffer = new char[1024];
        int len = 0;
        while ((len = req.getReader().read(buffer)) > -1)
        {
            body.append(buffer, 0, len);
        }
        System.out.println("Receiving web hook '" + eventIdentifier + "' with body\n" + body.toString());
        publications.add(new Publication(eventIdentifier, body.toString()));
    }

    @Override
    protected synchronized void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("application/json");
        JSONArray result = new JSONArray();
        for (final Publication pub : publications)
        {
            result.add(new JSONObject(new HashMap<String, Object>()
            {{
                    put("event", pub.eventIdentifier);
                    put("body", parseObject(pub.body));
                }}));
        }
        resp.getWriter().write(result.toJSONString());
    }

    private static class Publication
    {
        private final String eventIdentifier;
        private final String body;

        public Publication(String eventIdentifier, String body)
        {
            this.eventIdentifier = eventIdentifier;
            this.body = body;
        }
    }
}
